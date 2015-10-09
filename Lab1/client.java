import java.io.*;
import java.net.*;

/**
 * This is the implementation of a simple Client which sends and 
 * receives Strings from a Server via sockets. The String received 
 * is in all caps.
 *
 * @author Kieran Fraser
 *           
 */

class Client{
	
	 public static void main(String[] args) {
       try{
			//Create a new socket on port 8000 using LocalHost as the IP address
			Socket clientSocket = new Socket("127.0.0.1", 8000);
			
			//Create a new PrintWriter and attach the sockets output stream
			PrintWriter pw = new PrintWriter(clientSocket.getOutputStream());
			
			//Add the GET request to the PrintWriter to send to the server including the user's two strings
			//taken as arguments from command prompt
			pw.println("GET /echo.php?message="+args[0]+"+"+args[1] +"HTTP/1.0\r\n");
			
			//send request down the stream to the server
			pw.flush();
			
			//Create a buffer reader to read in messages from the server and attach the InputStreamReader initialized with the 
			//socket's input stream
			BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));			
			
			//While the server is still sending messages, print them to the screen and loop
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
			
				//Only wish to print the strings sent to the server and subsequently sent back as caps
				if(inputLine.contains("HTTP/1.0")){
					System.out.println("echoing: " + inputLine.substring(0, inputLine.length()-10));
				}
			}
			
			//Close the socket after communication
			clientSocket.close();
		
		}catch(IOException e)
      {
         e.printStackTrace();
      }
	}
}
