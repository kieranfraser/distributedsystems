import java.io.*;
 import java.net.*;
 class TCPServer { 
 public static void main(String argv[]) throws Exception       {   
 String clientSentence;        
 String capitalizedSentence;      
 InetAddress addr = InetAddress.getByName("192.168.1.2");
 ServerSocket welcomeSocket = new ServerSocket(8000, 50, addr);   
 while(true)          {    
 Socket connectionSocket = welcomeSocket.accept();    
 BufferedReader inFromClient =                new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
 DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());        
 clientSentence = inFromClient.readLine();       
 System.out.println("Received: " + clientSentence);   
 capitalizedSentence = clientSentence.toUpperCase() + '\n';     
 outToClient.writeBytes(capitalizedSentence);        
 }      
 }
 } 