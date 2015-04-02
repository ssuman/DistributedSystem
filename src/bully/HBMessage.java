package bully;

import java.io.Serializable;

public class HBMessage implements Serializable{

	private static final long serialVersionUID = 1L;
	public String content;
	public String ipAddress;
	public int portNum;
	public String processId;
	
	public HBMessage(String content,String ipAddress,int portNum,String processId) {
		this.content=content;
		this.ipAddress=ipAddress;
		this.portNum=portNum;
		this.processId=processId;
	}
}
