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
import java.util.HashMap;
import java.util.concurrent.ThreadPoolExecutor;

public class Task implements Runnable{
	
	private Socket clientSock;
	private int port;
	private ThreadPoolExecutor exec;
	
	private String ipAddress;
	private final String STUDENT_NO = "39f20a6b16fedf8e18d0ac5b965ef175bb06b53317538707ffd281a5ac93c0bb";
	private HashMap<String, byte[]> fileServiceMap;
	

	private FileOutputStream fileOutputStream;
	private BufferedOutputStream bufferedOutputStream;
	
	public Task(Socket clientSock, int port, ThreadPoolExecutor exec, HashMap<String, byte[]> fileServiceMap) throws UnknownHostException{
		this.clientSock = clientSock;
		this.port = port;
		this.exec = exec;
		String ip = InetAddress.getLocalHost().toString();
		String[] ipSplit = ip.split("/");
		ipAddress = ipSplit[1];
		this.fileServiceMap = fileServiceMap;	
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

				// in this case we assume that "clientSays" contains the filename of the
				// file that has been requested by the client. It must also contain the operation.
				// The format that is being assumed is: "read:filename" for a read()
				// 										"write:filename" for a write()
				// Once the operation and filename is established a response is sent to the client-proxy
				// for confirmation that the file can be sent. The file is then stored in a HashMap
				// as a byte array with the filename as the key. If I have time I'll change this to an 
				// SQLite DB which will provide persistence. 
				
				String[] querySplit = clientSays.split(":");
				String operation = querySplit[0];
				String fileName = querySplit[1];
				
				if(operation.equals("write")){
					writeOperation(fileName);
				}
				else if(operation.equals("read")){
					readOperation(fileName);
				}
				else{
					System.out.println("Must be an error somewhere!!");
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Basic Server Test Response
	 * @param response
	 * @throws IOException
	 */
	private void sendResponse(String response) throws IOException{
		/*BufferedWriter writeOut = new BufferedWriter(new OutputStreamWriter(clientSock.getOutputStream()));
		writeOut.write(response);
		writeOut.flush();*/
		
		DataOutputStream writeOut = new DataOutputStream(clientSock.getOutputStream());  
		writeOut.writeBytes(response);
		System.out.print("sent response");
	}

	private void writeOperation(String fileName) throws IOException{
				
		sendResponse("clear to send file\n");
		
		// Receive file from the client
		byte [] fileByteArray  = new byte [6022386];
		DataInputStream inputStream = new DataInputStream(clientSock.getInputStream());
		/*int bytesRead = inputStream.read(fileByteArray,0,fileByteArray.length);
		int current = bytesRead;
		
		do {
			bytesRead =
		    inputStream.read(fileByteArray, current, (fileByteArray.length-current));
		    if(bytesRead >= 0) current += bytesRead;
		  } while(bytesRead > -1);*/
		
		int length = inputStream.readInt();                    // read length of incoming message
		if(length>0) {
		    fileByteArray = new byte[length];
		    inputStream.readFully(fileByteArray, 0, fileByteArray.length); // read the message
		}
		
		System.out.println("file read.");
		fileServiceMap.put(fileName, fileByteArray);
		System.out.println("file saved.");
	}
	
	private void readOperation(String fileName) throws IOException{
		
		System.out.println(fileName);
		System.out.println(fileServiceMap.size());
		byte [] fileToSend = fileServiceMap.get(fileName);
		if(fileToSend != null){
			System.out.println("Sending " + fileName + "(" + fileToSend.length + " bytes)");
	        DataOutputStream dOut = new DataOutputStream(clientSock.getOutputStream());
	        dOut.writeInt(fileToSend.length); // write length of the message
	        dOut.write(fileToSend);  
	        System.out.println("File Sent.");
		}
		else{
			System.out.println("File can't be found?");
		}
	}

}
