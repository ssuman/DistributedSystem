package bully;

import java.util.Date;

public class ProcessList {

	protected String processId;
	protected String ipAddress;
	protected int portNum;
	protected Date date;
	
	public ProcessList(String processId,String ipAddr,int portNum,Date date){
		this.processId=processId;
		this.ipAddress=ipAddr;
		this.portNum = portNum;
		this.date=date;
	}

	@Override
	public String toString() {
		return "Process List [processId=" + processId + ", ipAddress="
				+ ipAddress + ", portNum=" + portNum + ", date=" + date + "]";
	}
	
	
}
