package FileServer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import main.AuthenticatorMessage;
import main.MessageForClient;
import main.Serializer;
import main.ServiceMessage;
import main.TicketGrantingTicket;

public class ClientProxy implements ClientFileProxy{

	private FileInputStream fileInputStream;
	private BufferedInputStream bufferedInputStream;
	private OutputStream outputStream;
	
	
	// This will be changed to reflect the location of the lock service.
	private final static int LOCKSERVICEPORT = 2000;

	// This will be changed to reflect the location of the directory service.
	private final static String DIRSERVICE = "127.0.0.1";
	private final static int DIRSERVICEPORT = 6000;
	
	// This will be changed to reflect the location of the Security service.
	private final static int SECURITYPORT = 3000;
	
	// Algorithm used for encryption
	private final static String ALGO = "AES";
	
	@Override
	public void read(String fileName, String username, String password) throws UnknownHostException, IOException, InterruptedException {
		
		fileName = "./"+fileName;
		
		Socket lockServiceSocket = new Socket("127.0.0.1", LOCKSERVICEPORT);
	    System.out.println("Connecting to Lock Service...");
	    
	    // requesting access to file, see if it's locked (will put in timestamp feature if have time)
	    String lockQuery = "request:"+fileName+"\n"; 
		DataOutputStream outToLockService = new DataOutputStream(lockServiceSocket.getOutputStream());  
		outToLockService.writeBytes(lockQuery);
		System.out.println("waiting for Lock Service response: ");
		
		BufferedReader inFromLockService = new BufferedReader(new InputStreamReader(lockServiceSocket.getInputStream()));
		String lockServiceReply = inFromLockService.readLine();
		System.out.println("server reply: "+lockServiceReply);
		
		if(lockServiceReply.equals("granted")){
			// File Directory
			
			Socket dirServiceSocket = new Socket("127.0.0.1", DIRSERVICEPORT);
		    System.out.println("Connecting to Directory Service...");
		    
		    String dirServiceQuery = "read"+":"+fileName+"\n"; 
			DataOutputStream outToDirService = new DataOutputStream(dirServiceSocket.getOutputStream());  
			outToDirService.writeBytes(dirServiceQuery);
			System.out.println("waiting for Directory Service response: ");
			
			BufferedReader inFromDirService = new BufferedReader(new InputStreamReader(dirServiceSocket.getInputStream()));
			String dirServiceReply = inFromDirService.readLine();
			System.out.println("server reply: "+dirServiceReply);
			
			String[] splitResponse = dirServiceReply.split(":");
			String fileServiceIP = splitResponse[0];
			int fileServicePort = Integer.valueOf(splitResponse[1]);
			
			
			// File Service
			Socket fileServiceSocket = new Socket(fileServiceIP, fileServicePort);
		    System.out.println("Connecting...");
		    
		    String query = "read:"+fileName+"\n"; 
			DataOutputStream outToServer = new DataOutputStream(fileServiceSocket.getOutputStream());  
			outToServer.writeBytes(query);
			System.out.println("waiting for response: ");
			
			byte [] fileByteArray  = new byte [6022386];
			DataInputStream inputStream = new DataInputStream(fileServiceSocket.getInputStream());
			int length = inputStream.readInt();                    // read length of incoming message
			if(length>0) {
			    fileByteArray = new byte[length];
			    inputStream.readFully(fileByteArray, 0, fileByteArray.length); // read the message
			}
			
			System.out.println("file read.");
			
			File receivedFile = new File(fileName);
		    FileOutputStream fos = new FileOutputStream(receivedFile);
	        fos.write(fileByteArray);
	        fos.flush();
	        fos.close();
	        
	        // open file in default application editor
	        String command = "cmd /C start "+fileName;
	    	Process openFile = Runtime.getRuntime().exec("notepad "+ fileName);
		}
		else if(lockServiceReply.equals("denied")){
			System.out.println("The file requested is locked by another user.");
		}
		else{
			System.out.println("Error reply from lock service.");
		}
		
    	
	}

	@Override
	public void write(String fileName, String username, String password) throws IOException, URISyntaxException {
	    

		fileName = "./"+fileName;
		String fileLocation = "127.0.0.1";
		sendToFileService(fileName, username, password);
	}
	
	private void sendToFileService(String fileName, String username, String password) throws UnknownHostException, IOException, URISyntaxException{
		
		// Security
		
		// Talk to the Kerberos authenticator
		String ip = InetAddress.getLocalHost().toString();
		String[] ipSplit = ip.split("/");
		String ipAddress = ipSplit[1];
		System.out.println(ipAddress);
		
		
		Socket securityServiceSocket = new Socket("127.0.0.1", SECURITYPORT);
	    System.out.println("Connecting to Security Service...");
	    
	    // 1. send plain-text request including: id, TGS ID, ipaddress, lifetime
	    // query in the form username:password:service:ipAddress:lifetime
	    // in this case -> username:password:TGS:ipAddress:null
	    String securityServiceQuery = 	username+":"+
	    								password+":"+
	    								"TGS"+":"+
	    								ipAddress+":"+
	    								"null"+"\n"; 
		DataOutputStream outToSecurityService = new DataOutputStream(securityServiceSocket.getOutputStream());  
		outToSecurityService.writeBytes(securityServiceQuery);
		System.out.println("waiting for Security Service response: ");
		
		// Receive back:  1. message containing the session key and service location encoded with the clientSecretKey
		//                2. ticket granting ticket to be sent to the service
		BufferedReader inFromSecurityService = new BufferedReader(new InputStreamReader(securityServiceSocket.getInputStream()));
		String securityReply = inFromSecurityService.readLine();
		String[] splitReply = securityReply.split(":");
		String encodedMessage = splitReply[0];
		String encodedTGT = splitReply[1];
		
		// Decrypt Message
		String clientIdentity = username+password;
		clientIdentity = clientIdentity.substring(0,16);
		Key clientSecretKey = null;
		MessageForClient decodedMessage = null;
		try {
			clientSecretKey = generateKey(clientIdentity.getBytes(), ALGO);
			byte[] decodedMessageArray = decrypt(clientSecretKey, encodedMessage);
	        decodedMessage = (MessageForClient) Serializer.deserialize(decodedMessageArray);
		} catch (Exception e) {}
		System.out.println("Message decoded successfully\n Service name: "+decodedMessage.getTgsName());
		
		// Get the TGS Session Key from the message which will be used for encrypting further communication 
		Key tgsSessionKey = decodedMessage.getSessionKeyTGS();
		
		System.out.println("message: "+encodedMessage);
		System.out.println("ticket: "+encodedTGT);
		
		// Prepare an authenticator message to send to the Ticket Granting Service
		Date timestamp = new Date();
		AuthenticatorMessage authenticator = new AuthenticatorMessage(username, password, timestamp);
		//Encrypt the Authenticator Message with the TGS session Key
		String encodedAuthenticator = null;
		try {
			encodedAuthenticator = encryptAuthenticator(tgsSessionKey, authenticator);
		} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException
				| NoSuchPaddingException e) {}
		
		// Prepare a plain text request which includes the service which we're asking access (could also include the lifetime 
		// access should be granted)
		// In a fully complete version, the service would point to the directory service or the lock service or the file service
		// but in this implementation, to save time, I'm just implementing security for communication to and from the directory service.
		
		String requestToTGS = "directoryService";
		
		// Prepare everything to send to the Ticket Granting Service and send - message in the form -  plainTextRequest:Authenticator:TicketGrantingTicket
		requestToTGS = requestToTGS+":"+encodedAuthenticator+":"+encodedTGT+"\n";
		
		securityServiceSocket = new Socket("127.0.0.1", SECURITYPORT);
	    System.out.println("Connecting to Security Service...");
	    
		outToSecurityService = new DataOutputStream(securityServiceSocket.getOutputStream());  
		outToSecurityService.writeBytes(requestToTGS);
		System.out.println("waiting for Security Service response: ");
		
		// Receive back from the TGS: 1. Service Session Key (Encrypted with the TGS session Key - can decrypt)
		//                            2. Ticket for Service (Encrypted with the Service Secret Key - cannot decrypt)
		inFromSecurityService = new BufferedReader(new InputStreamReader(securityServiceSocket.getInputStream()));
		securityReply = inFromSecurityService.readLine();
		splitReply = securityReply.split(":");
		String encodedServiceMessage = splitReply[0];
		String encodedServiceTicket = splitReply[1];
		
		//decrypt the encrypted Service message using the TGS session key to get the Service session key
		ServiceMessage decodedServiceMessage = null;
		try {
			byte[] decodedServiceMessageArray = decrypt(tgsSessionKey, encodedServiceMessage);
	        decodedServiceMessage = (ServiceMessage) Serializer.deserialize(decodedServiceMessageArray);
		} catch (Exception e) {
			System.out.println("errrrr");
		}
		System.out.println(decodedServiceMessage.getService());
		Key serviceSessionKey = decodedServiceMessage.getServiceSessionKey();
		
		//Send to the Service in question: 1. Ticket for Service 
		//                                 2. Authenticator (encrypted with the TGS session Key)
		// Note: in this case I'm only securing communication to the directory service. If I have more time I will extend this
		// to all services
		authenticator = new AuthenticatorMessage(username, password, new Date());
		try {
			encodedAuthenticator = encryptAuthenticator(serviceSessionKey, authenticator);
		} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException
				| NoSuchPaddingException e) {} 
		
		Socket dirServiceSocket = new Socket("127.0.0.1", DIRSERVICEPORT);
	    System.out.println("Connecting to Directory Service authenticator...");
		String message = "auth:"+encodedAuthenticator+":"+encodedServiceTicket+"\n";
		
		DataOutputStream outToDirService = new DataOutputStream(dirServiceSocket.getOutputStream());  
		outToDirService.writeBytes(message);
		System.out.println("waiting for Directory Service auth response: ");
		
		// Receive back from the Service an authenticator 
		BufferedReader inFromDirService = new BufferedReader(new InputStreamReader(dirServiceSocket.getInputStream()));
		String encodedAuth = inFromDirService.readLine();
		AuthenticatorMessage serviceAuth = null;
		
		try {
			byte[] serviceAuthArray = decrypt(serviceSessionKey, encodedAuth);
			serviceAuth = (AuthenticatorMessage) Serializer.deserialize(serviceAuthArray);
		} catch (Exception e) {}
		
		if(serviceAuth.getUsername().equals("directory")){
			// Security for this service done!
			// File Directory
			
			dirServiceSocket = new Socket("127.0.0.1", DIRSERVICEPORT);
		    System.out.println("Connecting to Directory Service...");
			
		    String dirServiceQuery = "write"+":"+fileName+"\n"; 
			outToDirService = new DataOutputStream(dirServiceSocket.getOutputStream());  
			outToDirService.writeBytes(dirServiceQuery);
			System.out.println("waiting for Directory Service response: ");
			
			inFromDirService = new BufferedReader(new InputStreamReader(dirServiceSocket.getInputStream()));
			String dirServiceReply = inFromDirService.readLine();
			System.out.println("server reply: "+dirServiceReply);
			
			String[] splitResponse = dirServiceReply.split(":");
			String fileServiceIP = splitResponse[0];
			int fileServicePort = Integer.valueOf(splitResponse[1]);
			
			// File Service
			Socket lockServiceSocket = new Socket(fileServiceIP, LOCKSERVICEPORT);
		    System.out.println("Connecting to Lock Service...");
			// requesting access to file, see if it's locked (will put in timestamp feature if have time)
		    String lockQuery = "request:"+fileName+"\n"; 
			DataOutputStream outToLockService = new DataOutputStream(lockServiceSocket.getOutputStream());  
			outToLockService.writeBytes(lockQuery);
			System.out.println("waiting for Lock Service response: ");
			
			BufferedReader inFromLockService = new BufferedReader(new InputStreamReader(lockServiceSocket.getInputStream()));
			String lockServiceReply = inFromLockService.readLine();
			System.out.println("server reply: "+lockServiceReply);
			
			if(lockServiceReply.equals("granted")){
				Socket socket = new Socket(fileServiceIP, fileServicePort);
			    System.out.println("Connecting...");
			    
			    String query = "write:"+fileName+"\n"; 
				DataOutputStream outToServer = new DataOutputStream(socket.getOutputStream());  
				outToServer.writeBytes(query);
				System.out.println("waiting for response: ");
				
				BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String reply = inFromServer.readLine();
				System.out.println("server reply: "+reply);

				//System.out.println(getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
				//String fileNameNew = getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath()+fileName;
				File fileToSend = new File(fileName);
				
				//System.out.println(ClientProxy.class.getClassLoader().getResource(fileName).getPath());
				//File fileToSend = new File(ClientProxy.class.getClassLoader().getResource(fileName).getPath());
				byte [] mybytearray  = new byte [(int)fileToSend.length()];
		        fileInputStream = new FileInputStream(fileToSend);
		        bufferedInputStream = new BufferedInputStream(fileInputStream);
		        bufferedInputStream.read(mybytearray,0,mybytearray.length);
		        //outputStream = socket.getOutputStream();
		        System.out.println("Sending " + fileName + "(" + mybytearray.length + " bytes)");
		        //outputStream.write(mybytearray,0,mybytearray.length);
		        //outputStream.flush();
		        DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());

		        dOut.writeInt(mybytearray.length); // write length of the message
		        dOut.write(mybytearray);  
		        System.out.println("File Sent.");
			    
			    // requesting access to file, see if it's locked (will put in timestamp feature if have time)
			    lockQuery = "release:"+fileName+"\n"; 
				outToLockService = new DataOutputStream(lockServiceSocket.getOutputStream());  
				outToLockService.writeBytes(lockQuery);
				System.out.println("File release message sent.");
			}
			else if(lockServiceReply.equals("denied")){
				System.out.println("The file requested is locked by another user.");
			}
			else{
				System.out.println("Error reply from lock service.");
			}
		}
		else{
			System.out.println("Could not authorize.");
		}        
	}
	
	private static Key generateKey(byte[] keyValue, String algo) throws Exception {
        Key key = new SecretKeySpec(keyValue, algo);
        return key;
	}
	
	private static byte[] decrypt(Key key, String message) throws Exception {
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.DECRYPT_MODE, key);
        Decoder decoder = Base64.getDecoder();
        byte[] decodedValue = decoder.decode(message);
        byte[] decValue = c.doFinal(decodedValue);
        return decValue;
    }
	
	private String encryptAuthenticator(Key key, AuthenticatorMessage message) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException, NoSuchAlgorithmException, NoSuchPaddingException{
		Cipher c = Cipher.getInstance(ALGO);
		c.init(Cipher.ENCRYPT_MODE, key);
		byte[] encVal = c.doFinal(Serializer.serialize(message));
		//String encryptedValue = new BASE64Encoder().encode(encVal);
		Encoder encoder = Base64.getEncoder();
		String encryptedValue = encoder.encodeToString(encVal);
		return encryptedValue;
	}

}
