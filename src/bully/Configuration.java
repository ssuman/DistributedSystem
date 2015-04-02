package bully;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Configuration {
	public final static String GROUP_ADDR = "230.0.0.1";
	public final static int PORT_NUM = 6000;
	public static final int HEARTBEAT_TIME = 1000;
	public static final int CHECK_TIME = 5000;
	public final static int PACKET_SIZE = 65003;
	public final static int TCP_PORT_NUM = 6100;
	public static String processID;
	public static long timeOutPeriod;
	public static volatile ArrayList<ProcessList> proc;
	public volatile static String leader;
	public volatile static boolean election;
	
	public  static void sendData(String ipAddress,String message){
		try{
			Socket socket = new Socket(ipAddress,Configuration.TCP_PORT_NUM);
			ObjectOutputStream ois = new ObjectOutputStream(socket.getOutputStream());
			ois.writeObject(new HBMessage(message,InetAddress.getLocalHost().getHostName(),Configuration.TCP_PORT_NUM,Configuration.processID));
			ois.close();
			socket.close();
			}catch(UnknownHostException err){
				System.out.println("Couldn't connect to: "+ipAddress);
			} catch(IOException err){
				System.out.println("Couldn't connect to " +ipAddress);
			}
	}
	
}
