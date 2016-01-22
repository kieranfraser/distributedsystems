package main;

import java.io.Serializable;
import java.security.Key;
import java.util.Date;

public class TicketGrantingTicket implements Serializable{

	private static final long serialVersionUID = 529718703194499770L;
	
	private String username;
	private String password;
	private Date timeStamp;
	private String ipAddress;
	private String lifetime;
	private Key sessionKeyTGS;
	
	public TicketGrantingTicket(String username, String password, Date timeStamp, String ipAddress, Key sessionKeyTGS){
		this.username = username;
		this.password = password;
		this.timeStamp = timeStamp;
		this.ipAddress = ipAddress;
		this.sessionKeyTGS = sessionKeyTGS;
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
	public Date getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}
	public String getIpAddress() {
		return ipAddress;
	}
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	public String getLifetime() {
		return lifetime;
	}
	public void setLifetime(String lifetime) {
		this.lifetime = lifetime;
	}
	public Key getSessionKeyTGS() {
		return sessionKeyTGS;
	}
	public void setSessionKeyTGS(Key sessionKeyTGS) {
		this.sessionKeyTGS = sessionKeyTGS;
	}
	
}
