package FileServer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

public interface ClientFileProxy {

	public void read(String fileName, String username, String password) throws UnknownHostException, IOException, InterruptedException;
	
	public void write(String fileName, String username, String password) throws IOException, URISyntaxException;
	
}
