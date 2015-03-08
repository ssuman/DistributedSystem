

import java.io.Serializable;

public class Neighbor implements Serializable {

	private static final long serialVersionUID = 1L;
	private String VID;
	private String ipAddress;
	private int portNo;
	
	public Neighbor(String VID,String ipAddress,int portNo){
		this.VID=VID;
		this.ipAddress=ipAddress;
		this.portNo=portNo;
	}
	
	public Neighbor(String VID){
		this.VID=VID;
	}
	
	public String getVID(){
		return VID;
	}
	
	public String getIpAddress(){
		return ipAddress;
	}
	
	public int getPortNo(){
		return portNo;
	}
	public void setVid(String vid){
		this.VID=vid;
	}
	
	
	
}
