package main;

import java.io.Serializable;
import java.security.Key;
import java.util.Date;

public class ServiceTicket implements Serializable{
	
	private static final long serialVersionUID = -5704847934506717481L;
	
	
	private String username;
	private String password;
	private String service;
	private String ipAddress;
	private Date timestamp;
	private Key serviceSessionKey;
	
	public ServiceTicket(String username, String password, String service, String ipAddress, Date timestamp, Key serviceSessionKey){
		this.username = username;
		this.password = password;
		this.service = service;
		this.timestamp = timestamp;
		this.ipAddress = ipAddress;
		this.serviceSessionKey = serviceSessionKey;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public Key getServiceSessionKey() {
		return serviceSessionKey;
	}

	public void setServiceSessionKey(Key serviceSessionKey) {
		this.serviceSessionKey = serviceSessionKey;
	}
	

}
