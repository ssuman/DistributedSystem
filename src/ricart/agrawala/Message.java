package ricart.agrawala;

import java.io.Serializable;

public class Message implements Serializable{

	private static final long serialVersionUID = 1L;
	
	public String ipAddress;
	public int portNum;
	public int processId;
	public String content;
	
	public Message(String content,String ipAddress,int portNum,int processId) {
		
		this.content=content;
		this.ipAddress=ipAddress;
		this.portNum=portNum;
		this.processId=processId;
	}
	
	
}
