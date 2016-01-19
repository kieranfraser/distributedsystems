package main;

import java.util.Date;

public class Semaphore {
	
	private boolean locked;
	private String clientIP;
	private Date expires;
	
	public Semaphore(boolean locked, String clientIP, Date expires){
		this.locked = locked;
		this.clientIP = clientIP;
	}
	
	public boolean isLocked() {
		return locked;
	}
	public void setLocked(boolean locked) {
		this.locked = locked;
	}
	public String getClientIP() {
		return clientIP;
	}
	public void setClientIP(String clientIP) {
		this.clientIP = clientIP;
	}
	public void setExpires(Date expires){
		long expireInTwoHours = expires.getTime() + 7200000;
		this.expires = new Date(expireInTwoHours);
	}
	public Date getExpires() {
		return expires;
	}
	public boolean expired(Date currentDate){
		boolean expired = false;
		long curTime = currentDate.getTime();
		long expiredTime = this.expires.getTime();
		long difference = curTime - expiredTime;
		if(difference>0){
			expired = true;
		}
		return expired;
	}

}
