package main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class FileService {
	
	private static int PORT = 8000;
	private static HashMap<String, byte[]> fileServiceMap;

	public static void main(String[] args) throws IOException{
			
		ServerSocket serverSock = new ServerSocket(PORT);
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
		System.out.print("File Service Started Successfully");
		fileServiceMap = new HashMap<String, byte[]>();	
			
			while(true){
				Socket connectionSocket = serverSock.accept();
				Task task = new Task(connectionSocket, PORT, executor, fileServiceMap);
		        System.out.println("A new task has been added");
		        executor.execute(task);
			}
	}
}
