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
import java.nio.channels.FileLock;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ThreadPoolExecutor;

public class Task implements Runnable{
	
	private Socket clientSock;
	private int port;
	private ThreadPoolExecutor exec;
	
	private String ipAddress;
	private final String STUDENT_NO = "39f20a6b16fedf8e18d0ac5b965ef175bb06b53317538707ffd281a5ac93c0bb";
	private HashMap<String, Semaphore> lockServiceMap;
		

	private FileOutputStream fileOutputStream;
	private BufferedOutputStream bufferedOutputStream;
	
	private String clientIP;
	
	public Task(Socket clientSock, int port, ThreadPoolExecutor exec, HashMap<String, Semaphore> lockServiceMap) throws UnknownHostException{
		this.clientSock = clientSock;
		this.port = port;
		this.exec = exec;
		String ip = InetAddress.getLocalHost().toString();
		String[] ipSplit = ip.split("/");
		ipAddress = ipSplit[1];
		System.out.println("Host ip address: "+ipAddress);
		this.lockServiceMap = lockServiceMap;	
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

				// in this case we assume that "clientSays" contains the request for
				// the lock a particular file. The first step is to check if this 
				// file is in the lock system records and if not, add it. Then, supply
				// the lock to the client based on whether it is locked or not. 
				// Might also be useful to introduce a timeout feature on the lock in order to 
				// prevent a client getting and keeping a lock.
				// SQLite DB which will provide persistence. 
				// query will be in the form of: "request:filename" or "released:filename"
				// depending on whether or not the client is wanting or releasing the lock.
				
				String[] querySplit = clientSays.split(":");
				String operation = querySplit[0];
				String fileName = querySplit[1];
				String clientIPFull = clientSock.getInetAddress().getLocalHost().toString();
				String[] clientIpSplit = clientIPFull.split("/");
				clientIP = clientIpSplit[1];
				if(operation.equals("request")){
					requestOperation(fileName);
				}
				else if(operation.equals("release")){
					releaseOperation(fileName);
				}
				else{
					System.out.println("Must be a lock error somewhere!!");
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

	private void requestOperation(String fileName) throws IOException{
		
		// check if filename exists
		// if it doesn't add it and set to locked
		// if it does, check if locked - not grant access and lock - is deny access
		// note: true = unlocked, false = locked 							 
		
		Semaphore fileLock = lockServiceMap.get(fileName);
		if(fileLock != null){
			if(!fileLock.isLocked()){
				System.out.println("File unlocked, locking..\n");
				fileLock.setLocked(true);
				fileLock.setClientIP(clientIP);
				fileLock.setExpires(new Date());
				lockServiceMap.put(fileName, fileLock);
				sendResponse("granted\n");
			}
			else{
				System.out.println("File is locked\n");
				if(fileLock.getClientIP().equals(clientIP)){
					System.out.println("File locked, but has permission. Expiry updated.");
					fileLock.setExpires(new Date());
					lockServiceMap.put(fileName, fileLock);
					sendResponse("granted\n");
				}
				else{
					if(fileLock.expired(new Date())){
						System.out.println("File locked but token expired. Client given token");
						fileLock.setLocked(true);
						fileLock.setClientIP(clientIP);
						fileLock.setExpires(new Date());
						lockServiceMap.put(fileName, fileLock);
						sendResponse("granted\n");
					}
					else{
						System.out.println("Access Denied. File locked.");
						sendResponse("denied\n");
					}
				}
			}
		}
		else{
			System.out.println("New file added to locking system, and now locked\n");
			lockServiceMap.put(fileName, new Semaphore(true, clientIP, new Date()));
			sendResponse("granted\n");
		}
		
	}
	
	private void releaseOperation(String fileName) throws IOException{
		Semaphore fileLock = lockServiceMap.get(fileName);
		if(fileLock != null){
			if(!fileLock.isLocked()){
				System.out.println("File wasn't locked\n");
				lockServiceMap.put(fileName, new Semaphore(false, clientIP, new Date()));
			}
			else{
				if(clientIP.equals(fileLock.getClientIP())){
					System.out.println("File unlocked\n");
					fileLock.setLocked(false);
					lockServiceMap.put(fileName, fileLock);
				}
			}
		}
		else{
			System.out.println("New file added to locking system\n");
			lockServiceMap.put(fileName, new Semaphore(false, clientIP, new Date()));
		}
	}

}
