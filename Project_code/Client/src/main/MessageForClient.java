package main;

import java.io.Serializable;
import java.security.Key;
import java.util.Date;

public class MessageForClient implements Serializable{
	
	private static final long serialVersionUID = 2477344795153036864L;
	
	private String tgsName;
	private Date timeStamp;
	private Date lifetime;
	private Key sessionKeyTGS;
	
	public MessageForClient(String tgsName, Date timeStamp, Key sessionKeyTGS){
		this.tgsName = tgsName;
		this.timeStamp = timeStamp;
		this.sessionKeyTGS = sessionKeyTGS;
	}
	
	public String getTgsName() {
		return tgsName;
	}
	public void setTgsName(String tgsName) {
		this.tgsName = tgsName;
	}
	public Date getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}
	public Date getLifetime() {
		return lifetime;
	}
	public void setLifetime(Date lifetime) {
		this.lifetime = lifetime;
	}
	public Key getSessionKeyTGS() {
		return sessionKeyTGS;
	}
	public void setSessionKeyTGS(Key sessionKeyTGS) {
		this.sessionKeyTGS = sessionKeyTGS;
	}
	
	
}
