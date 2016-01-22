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
import java.util.HashMap;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.Date;
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
	private HashMap<String, LocationDetail> directory;

	private final static String ALGO = "AES";
	private final static String SERVICE = "directory";
	private Key serviceSessionKey;
	
	// the secret key shared between the security service and the directory service
	private final byte[] DIR_SERVICE_SECRET_KEY_VALUE = new byte[] {'d','i','r','e','c','t','o','r','y','s','e','r','v','i','c','e'};
	

	private FileOutputStream fileOutputStream;
	private BufferedOutputStream bufferedOutputStream;
	
	public Task(Socket clientSock, int port, ThreadPoolExecutor exec, HashMap<String, LocationDetail> directory) throws UnknownHostException{
		this.clientSock = clientSock;
		this.port = port;
		this.exec = exec;
		String ip = InetAddress.getLocalHost().toString();
		String[] ipSplit = ip.split("/");
		ipAddress = ipSplit[1];
		this.directory = directory;	
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
				String operation = querySplit[0];
				String fileName = querySplit[1];
				// in this case we assume that "clientSays" contains the filename of the
				// file that has been requested by the client. The response to the client
				// should contain the server location and the filename on that server.
				
				if(operation.equals("auth")){
					authorizeClient(querySplit);
					// send back authenticator to client
					AuthenticatorMessage auth = new AuthenticatorMessage(SERVICE, null, new Date());
					String encodedAuth = encryptAuth(serviceSessionKey, auth);
					sendResponse(encodedAuth+"\n");
				}
				else{
					// check if file is in the directory and return location if it is
					LocationDetail fileLocation = directory.get(fileName);
					if(fileLocation != null){
						System.out.println("File found in Directory.");
						sendResponse(fileLocation.getIpAddress()+":"+fileLocation.getPort()+"\n");
					}
					else{
						if(operation.equals("write")){
							System.out.println("New file added to directoy.");
							// method for searching all servers available to place new file
							// some sort of load balancing algorithm needed. Will come back to it
							// if have time.
							LocationDetail locationDetail = new LocationDetail("127.0.0.1", 8000);
							directory.put(fileName, locationDetail);
							sendResponse(locationDetail.getIpAddress()+":"+locationDetail.getPort()+"\n");
						}
						else{
							System.out.println("The file was not found in the Directory.");
							sendResponse("not found\n");
						}
					}
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
	
	private boolean authorizeClient(String[] query) throws Exception{
		String encodedAuthenticator = query[1];
		String encodedServiceTicket = query[2];
		// Generate the secret key shared between the security service and this service
		Key serviceSecretKey = generateKey(DIR_SERVICE_SECRET_KEY_VALUE, ALGO);
		// Decrypt the Service ticket with the secret key
		byte[] serviceTicketArray = decrypt(serviceSecretKey, encodedServiceTicket);
		ServiceTicket serviceTicket = (ServiceTicket) Serializer.deserialize(serviceTicketArray);
		// Decrypt the authenticator using the Service session key stored in the ticket.
		serviceSessionKey = serviceTicket.getServiceSessionKey();
		byte[] authenticatorArray = decrypt(serviceSessionKey, encodedAuthenticator);
		AuthenticatorMessage authenticator = (AuthenticatorMessage) Serializer.deserialize(authenticatorArray);
		// check the username and password of both the authenticator and ticket match
		if(authenticator.getUsername().equals(serviceTicket.getUsername())){
			if(authenticator.getPassword().equals(serviceTicket.getPassword())){
				return true;
			}
			else return false;
		}
		else return false;
	}
	
	/**
	 * Decrypt given data with key and return
	 */
	private static byte[] decrypt(Key key, String message) throws Exception {
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.DECRYPT_MODE, key);
        Decoder decoder = Base64.getDecoder();
        byte[] decodedValue = decoder.decode(message);
        byte[] decValue = c.doFinal(decodedValue);
        return decValue;
    }
	
	private String encryptAuth(Key key, AuthenticatorMessage message) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException, NoSuchAlgorithmException, NoSuchPaddingException{
		Cipher c = Cipher.getInstance(ALGO);
		c.init(Cipher.ENCRYPT_MODE, key);
		byte[] encVal = c.doFinal(Serializer.serialize(message));
		Encoder encoder = Base64.getEncoder();
		String encryptedValue = encoder.encodeToString(encVal);
		return encryptedValue;
	}
	
	private static Key generateKey(byte[] keyValue, String algo) throws Exception {
        Key key = new SecretKeySpec(keyValue, algo);
        return key;
	}

}
