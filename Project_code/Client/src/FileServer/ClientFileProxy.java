package FileServer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

public interface ClientFileProxy {

	public void read(String fileName) throws UnknownHostException, IOException, InterruptedException;
	
	public void write(String fileName) throws IOException, URISyntaxException;
	
}
