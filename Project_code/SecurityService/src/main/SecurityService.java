package main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class SecurityService {

	private static int PORT = 3000;
	private static HashMap<String, String> authClientDB;
	private static HashMap<String, String> tgsServiceDB;

	public static void main(String[] args) throws IOException{
			
		ServerSocket serverSock = new ServerSocket(PORT);
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
		System.out.print("Security Service Started Successfully");
		authClientDB = new HashMap<>();
		tgsServiceDB = new HashMap<>();
		
		// Put all existing services into the Ticket Granting Service database
		tgsServiceDB.put("directoryService", "address");
		tgsServiceDB.put("lock", "address");
		tgsServiceDB.put("File Server", "address");
			
			while(true){
				Socket connectionSocket = serverSock.accept();
				Task task = new Task(connectionSocket, PORT, executor, authClientDB, tgsServiceDB);
		        System.out.println("A new task has been added");
		        executor.execute(task);
			}
	}
}
