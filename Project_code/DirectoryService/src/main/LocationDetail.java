package main;

public class LocationDetail {

	private String ipAddress;
	private int port;
	
	public LocationDetail(String ipAddress, int port){
		this.ipAddress = ipAddress;
		this.port = port;
	}
	
	public String getIpAddress() {
		return ipAddress;
	}
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	
}
