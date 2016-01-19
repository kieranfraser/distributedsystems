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
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

public class ClientProxy implements ClientFileProxy{

	private FileInputStream fileInputStream;
	private BufferedInputStream bufferedInputStream;
	private OutputStream outputStream;
	
	
	// This will be changed to reflect the location of the lock service.
	private final static int LOCKSERVICEPORT = 2000;

	// This will be changed to reflect the location of the directory service.
	private final static String DIRSERVICE = "127.0.0.1";
	private final static int DIRSERVICEPORT = 6000;
	
	@Override
	public void read(String fileName) throws UnknownHostException, IOException, InterruptedException {
		
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
	public void write(String fileName) throws IOException, URISyntaxException {
	    

		fileName = "./"+fileName;
		String fileLocation = "127.0.0.1";
		sendToFileService(fileName);
	}
	
	private void sendToFileService(String fileName) throws UnknownHostException, IOException, URISyntaxException{
		
		// File Directory
		
		Socket dirServiceSocket = new Socket("127.0.0.1", DIRSERVICEPORT);
	    System.out.println("Connecting to Directory Service...");
	    
	    String dirServiceQuery = "write"+":"+fileName+"\n"; 
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

}
