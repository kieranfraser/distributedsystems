package main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * The lock service works as follow:
 * 
 * A client wishing to read a file will first contact the lock service
 * to check if the file is available. If it isn't locked access will be 
 * "granted" and the client can proceed. If the file is locked, the client
 * ip is checked with that of who locked the file. If it is the same client
 * access is granted, if not access is denied (unless the expiry datetime of 
 * the token has expired in which case the client will gain access and the 
 * file will be locked in that clients name).
 * 
 * If a client is looking for a file that the lock service is unaware of
 * the file will be added to the lock service directory and access is granted
 * immediately.
 * 
 * @author kfraser
 *
 */
public class LockService {
	
	private static int PORT = 2000;
	private static HashMap<String, Semaphore> lockServiceMap;

	public static void main(String[] args) throws IOException{
			
		ServerSocket serverSock = new ServerSocket(PORT);
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
		System.out.print("Lock Service Started Successfully");
		lockServiceMap = new HashMap<String, Semaphore>();	
			
			while(true){
				Socket connectionSocket = serverSock.accept();
				Task task = new Task(connectionSocket, PORT, executor, lockServiceMap);
		        System.out.println("A new task has been added");
		        executor.execute(task);
			}
	}
}
