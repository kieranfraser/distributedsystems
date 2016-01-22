package main;

import java.io.Serializable;
import java.security.Key;
import java.util.Date;

public class ServiceMessage implements Serializable{
	
	private static final long serialVersionUID = -1670024500896065425L;
	
	private String service;
	private Date timestamp;
	private Key serviceSessionKey;
	
	public ServiceMessage(String service, Date timestamp, Key serviceSessionKey){
		this.service = service;
		this.timestamp = timestamp;
		this.serviceSessionKey = serviceSessionKey;
	}
	
	public String getService() {
		return service;
	}
	public void setService(String service) {
		this.service = service;
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
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
}
