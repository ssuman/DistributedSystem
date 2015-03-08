

import java.io.Serializable;
import java.util.List;

public class Message implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Point point;
	private String message;
	protected String payLoad;
	private String ipAddress;
	private int portNo;
	protected List<Neighbor> Neighbor;
	
	public Message(Point point, String message,String ipAddress,int portNo){
		this.point=point;
		this.ipAddress=ipAddress;
		this.portNo=portNo;
		this.message=message;
		
	}
	
	public Message(String action,String payLoad,String ipAddress,int portNo) {
		this.message=action;
		this.payLoad=payLoad;
		this.ipAddress=ipAddress;
		this.portNo=portNo;
	}
	
	public Message(String action,String payLoad,String ipAddress,int portNo,List<Neighbor> N) {
		this.message=action;
		this.payLoad=payLoad;
		this.ipAddress=ipAddress;
		this.portNo=portNo;
		this.Neighbor=N;
	}

	public String getMessage(){
		return message;
	}
	public void setMessage(String messg){
		this.message=messg;
	}
	
	public Point getPoint(){
		return point;
	}
	
	public String getIpAdd(){
		return ipAddress;
	}
	
	public int getPortNo(){
		return portNo;
	}
}
