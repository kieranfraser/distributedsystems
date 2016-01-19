package main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class DirectoryService {
	
	private static int PORT = 6000;
	private static HashMap<String, LocationDetail> directory;

	public static void main(String[] args) throws IOException{
			
		ServerSocket serverSock = new ServerSocket(PORT);
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
		System.out.print("Directory Service Started Successfully");
		directory = new HashMap<String, LocationDetail>();	
			
			while(true){
				Socket connectionSocket = serverSock.accept();
				Task task = new Task(connectionSocket, PORT, executor, directory);
		        System.out.println("A new task has been added");
		        executor.execute(task);
			}
	}
}
