package FileServer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class FileServerMain {

	private String hostName;
	private static final int PORT = 2020;
	private final int threadMax = 10;
	private static ClientProxy clientProxy;
	
	public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException{
		

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        clientProxy = new ClientProxy();
        boolean loggedIn = false;
        String username = null;
        String password = null;
        
        while(true){
        	if(loggedIn == false){
        		System.out.print("Enter Username (must be at least 10 characters):\n");
                username = br.readLine();
                username = username.trim();
                System.out.print("Enter Password (must be at least 6 characters):\n");
                password = br.readLine();
                password = password.trim();
                loggedIn = true;
        	}
        	else{
        		System.out.print("read or write?\n");
                String s = br.readLine();
                s = s.trim();
                if(s.equals("read") || s.equals("r")){
                	System.out.println("Enter file to read: \n");
                	String fileName = br.readLine();
                	clientProxy.read(fileName, username, password);
                	
                }
                else if(s.equals("write") || s.equals("w")){
                	System.out.println("Enter file path: \n");
                	String fileName = br.readLine();
                	System.out.println(fileName);
                	clientProxy.write(fileName, username, password);
                }
                else{
                	System.out.println("Not Valid Input");
                }
        	}
        	
        	
        }
        
		
	}
}
