package main;

import java.io.Serializable;
import java.util.Date;

public class AuthenticatorMessage implements Serializable{

	private static final long serialVersionUID = -7298634959296580962L;
	
	private String username;
	private String password;
	private Date timestamp;
	
	public AuthenticatorMessage(String username, String password, Date timestamp){
		this.username = username;
		this.password = password;
		this.timestamp = timestamp;
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
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	
	
}
