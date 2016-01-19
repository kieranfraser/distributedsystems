package FileServer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

public class LabTwoTest implements Runnable{

	private Socket clientSock;
	private int port;
	private ThreadPoolExecutor exec;

	private final String STUDENT_NO = "39f20a6b16fedf8e18d0ac5b965ef175bb06b53317538707ffd281a5ac93c0bb";
	private String ipAddress;
	
	public LabTwoTest(Socket clientSock, int port, ThreadPoolExecutor exec) throws UnknownHostException{
		this.clientSock = clientSock;
		this.port = port;
		this.exec = exec;
		String ip = InetAddress.getLocalHost().toString();
		String[] ipSplit = ip.split("/");
		ipAddress = ipSplit[1];
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		BufferedReader inFromClient;
		try {
			inFromClient = new BufferedReader(new InputStreamReader(clientSock.getInputStream()));
			String clientSays = inFromClient.readLine();
			System.out.println("Client says: "+clientSays);
			try {
			    Thread.sleep(5000);                 //1000 milliseconds is one second.
			} catch(InterruptedException ex) {
			    Thread.currentThread().interrupt();
			}
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
				//do nothing
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void sendResponse(String response) throws IOException{
		BufferedWriter writeOut = new BufferedWriter(new OutputStreamWriter(clientSock.getOutputStream()));
		writeOut.write(response);
		writeOut.flush();
		System.out.print("sent response");
	}

}
