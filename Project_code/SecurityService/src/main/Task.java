package main;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ThreadPoolExecutor;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class Task implements Runnable{
	
	private Socket clientSock;
	private int port;
	private ThreadPoolExecutor exec;
	
	private String ipAddress;
	private final String STUDENT_NO = "39f20a6b16fedf8e18d0ac5b965ef175bb06b53317538707ffd281a5ac93c0bb";
	

	private FileOutputStream fileOutputStream;
	private BufferedOutputStream bufferedOutputStream;
	
	private HashMap<String, String> authClientDB;
	private HashMap<String, String> tgsServiceDB;
	
	private final static String ALGO = "AES";
	// the secret key shared only between the Authenticator and the Ticket Granting Service
	private final byte[] TGS_SECRET_KEY_VALUE = new byte[] {'t','h','i','s','t','g','s','s','e','c','r','e','t','k','e','y'};
	// the secret key shared between the security service and the directory service
	private final byte[] DIR_SERVICE_SECRET_KEY_VALUE = new byte[] {'d','i','r','e','c','t','o','r','y','s','e','r','v','i','c','e'};
	
	public Task(Socket clientSock, int port, ThreadPoolExecutor exec, HashMap<String, String> authClientDB, HashMap<String, String> tgsServiceDB) throws UnknownHostException{
		this.clientSock = clientSock;
		this.port = port;
		this.exec = exec;
		String ip = InetAddress.getLocalHost().toString();
		String[] ipSplit = ip.split("/");
		ipAddress = ipSplit[1];
		this.authClientDB = authClientDB;
		this.tgsServiceDB = tgsServiceDB;
	}

	@Override
	public void run() {
		
		BufferedReader inFromClient;
		try {
			inFromClient = new BufferedReader(new InputStreamReader(clientSock.getInputStream()));
			String clientSays = inFromClient.readLine();
			
			System.out.println("Client says: "+clientSays);
			

			// Basic Server Test
			if(clientSays.contains("KILL_SERVICE")){
				clientSock.close();
				exec.shutdownNow();
				System.exit(0);
			}
			else if(clientSays.contains("HELO")){
				String response = clientSays+"\n";
				String ip = "IP:"+ipAddress+"\n";
				String prt = "Port:"+port+"\n";
				String id = "StudentID:"+STUDENT_NO+"\n";
				sendResponse(response+ip+prt+id);
			}
			else {

				String[] querySplit = clientSays.split(":");
				if(querySplit.length == 5){
					authenticator(querySplit);
				}
				else if(querySplit.length == 3){
					ticketGenerationService(querySplit);
				}
				else{
					System.out.println("Different size");
				}
				
				
				
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Basic Server Test Response
	 * @param response
	 * @throws IOException
	 */
	private void sendResponse(String response) throws IOException{
		DataOutputStream writeOut = new DataOutputStream(clientSock.getOutputStream());  
		writeOut.writeBytes(response);
		System.out.print("sent response");
	}
	
	/**
	 * TGS - This service grants access to various services once the 
	 * client has already been authenticated to the Security Service.
	 * @throws Exception 
	 */
	private void ticketGenerationService(String[] query) throws Exception{
		//Received from the client: message in the form -  plainTextRequest:Authenticator:TicketGrantingTicket
		String service = query[0];
		String encodedAuthenticator = query[1];
		String encodedTGT = query[2];
		
		// Check to make sure service exists
		String checkExist = tgsServiceDB.get(service);
		if(checkExist != null){
		
			// Generate the TGS secret key
			Key secretKeyTGS = generateKey(TGS_SECRET_KEY_VALUE, ALGO);
			// decrypt the Ticket Granting Ticket (encrypted with the secret key only we know which we have). This ticket contains the TGS session key.
			byte[] decodedTicketArray = decrypt(secretKeyTGS, encodedTGT);
			TicketGrantingTicket decodedTGT = (TicketGrantingTicket) Serializer.deserialize(decodedTicketArray);
			// decrypt the Authenticator (encrypted with the TGS session key which we now have as it was in the ticket).
			byte[] decodedAuthenticatorArray = decrypt(decodedTGT.getSessionKeyTGS(), encodedAuthenticator);
			AuthenticatorMessage decodedAuthenticator = (AuthenticatorMessage) Serializer.deserialize(decodedAuthenticatorArray);
			
			// Ensure the username and password in both the ticket and the authenticator match
			if(decodedAuthenticator.getUsername().equals(decodedTGT.getUsername())){
				if(decodedAuthenticator.getPassword().equals(decodedTGT.getPassword())){
					// Generate the Service secret key (Key shared between the servic and the security service only)
					Key serviceSecretKey = generateKey(DIR_SERVICE_SECRET_KEY_VALUE, ALGO);
					// Randomly generate the Service session key
					Key serviceSessionKey = generateSessionKey();
					Date timestamp = new Date();
					// Create the Service Ticket
					ServiceTicket serviceTicket = new ServiceTicket(decodedAuthenticator.getUsername(), decodedAuthenticator.getPassword(),
							service, null, timestamp, serviceSessionKey);
					// Encrypt the Service ticket with the Service secret key 
					String encodedServiceTicket = encryptServiceTicket(serviceSecretKey, serviceTicket);
					// Create the message to send to client
					ServiceMessage serviceMessage = new ServiceMessage(service, timestamp, serviceSessionKey);
					// Encrypt this message with the TGS session key
					String encodedMessage = encryptServiceMessage(decodedTGT.getSessionKeyTGS(), serviceMessage);
					// Prepare message to be sent to client. This is in the form of: 	ServiceMessage:ServiceTicket
					// The service message will be decryptable by the client, the ticket won't be.
					String response = encodedMessage+":"+encodedServiceTicket+"\n";
					sendResponse(response);
				}
				else{
					System.out.println("Authentication failed, password's don't match. Security breach!");
				}
			}
			else{
				System.out.println("Authentication failed, username's don't match. Security breach!");
			}
			
		}
		else{
			System.out.println("Service could not be found in the database.");
		}
		
	}
	
	/**
	 * Authenticator - this internal service first deals with the client
	 * @throws Exception 
	 */
	private void authenticator(String[] query) throws Exception{

		String username = query[0];
		String password = query[1];
		String service = query[2];
		String ip = query[3];
		String lifeTime = query[4];
		
		// Make sure user isn't in database already
		String checkExist = authClientDB.get(username);
		if(checkExist == null){
			// Not in the database so can proceed safely!
			Date timestamp = new Date();
			// Randomly generate session key for use between client and Ticket Granting Service (TGS)
			Key sessionKey = generateSessionKey();
			// Create the Ticket Granting Ticket (to be returned to the client)
			TicketGrantingTicket tgt = new TicketGrantingTicket(username, password, timestamp, ip, sessionKey);
			// Generate the TGS secret key
			Key secretKeyTGS = generateKey(TGS_SECRET_KEY_VALUE, ALGO);
			// Encrypt the TGT with the TTGS Secret Key
			String encodedTicket = encryptTGT(secretKeyTGS, tgt);
			// Generate the Client Secret Key (key shared between us and client, based on the username+password combination of client)
			String clientIdentity = username+password;
			clientIdentity = clientIdentity.substring(0, 16);
			Key clientSecretKey = generateKey(clientIdentity.getBytes(), ALGO);
			// Create other message
			MessageForClient otherMessage = new MessageForClient(service, timestamp, sessionKey);
			// Encrypt other message with Client Secret Key
			String encodedMessage = encryptMessage(clientSecretKey, otherMessage);
			// send the message to the client and TGT to the client in the form -  message:TGT
			String sendToClient = encodedMessage+":"+encodedTicket+"\n";
			sendResponse(sendToClient);
		}
		else{
			System.out.println("There's been an error somewhere!");
		}
		
	}
	
	private Key generateSessionKey() throws Exception{
		byte[] randomKeyValue = new byte[16];
		new Random().nextBytes(randomKeyValue);
		return generateKey(randomKeyValue, ALGO);
	}
	
	
	/** 
	 * Encrypt given data with key and return
	 * @throws InvalidKeyException 
	 * @throws IOException 
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchAlgorithmException 
	 */
	private String encryptTGT(Key key, TicketGrantingTicket tgt) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException, NoSuchAlgorithmException, NoSuchPaddingException{
		Cipher c = Cipher.getInstance(ALGO);
		c.init(Cipher.ENCRYPT_MODE, key);
		byte[] encVal = c.doFinal(Serializer.serialize(tgt));
		Encoder encoder = Base64.getEncoder();
		String encryptedValue = encoder.encodeToString(encVal);
		return encryptedValue;
	}
	
	private String encryptMessage(Key key, MessageForClient message) throws IllegalBlockSizeException, BadPaddingException, IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException{
		Cipher c = Cipher.getInstance(ALGO);
		c.init(Cipher.ENCRYPT_MODE, key);
		byte[] encVal = c.doFinal(Serializer.serialize(message));
		Encoder encoder = Base64.getEncoder();
		String encryptedValue = encoder.encodeToString(encVal);
		return encryptedValue;
	}
	
	private String encryptServiceMessage(Key key, ServiceMessage message) throws IllegalBlockSizeException, BadPaddingException, IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException{
		Cipher c = Cipher.getInstance(ALGO);
		c.init(Cipher.ENCRYPT_MODE, key);
		byte[] encVal = c.doFinal(Serializer.serialize(message));
		Encoder encoder = Base64.getEncoder();
		String encryptedValue = encoder.encodeToString(encVal);
		return encryptedValue;
	}
	
	private String encryptServiceTicket(Key key, ServiceTicket ticket) throws IllegalBlockSizeException, BadPaddingException, IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException{
		Cipher c = Cipher.getInstance(ALGO);
		c.init(Cipher.ENCRYPT_MODE, key);
		byte[] encVal = c.doFinal(Serializer.serialize(ticket));
		Encoder encoder = Base64.getEncoder();
		String encryptedValue = encoder.encodeToString(encVal);
		return encryptedValue;
	}
	
	/**
	 * Decrypt given data with key and return
	 */
	private static byte[] decrypt(Key key, String message) throws Exception {
		System.out.println(key.toString());
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.DECRYPT_MODE, key);
        Decoder decoder = Base64.getDecoder();
        byte[] decodedValue = decoder.decode(message);
        byte[] decValue = c.doFinal(decodedValue);
        return decValue;
    }
	
	/**
	 * Create a key for a given algorithm based on the key value
	 * Algo to be used is AES
	 * @param keyValue
	 * @param algo
	 * @return
	 * @throws Exception
	 */
	private static Key generateKey(byte[] keyValue, String algo) throws Exception {
	        Key key = new SecretKeySpec(keyValue, algo);
	        return key;
	}

}
