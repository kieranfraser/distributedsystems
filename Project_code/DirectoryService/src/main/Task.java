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
	private HashMap<String, LocationDetail> directory;
	

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
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		;
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

}
