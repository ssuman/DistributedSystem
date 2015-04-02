package ricart.agrawala;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;



public class Configuration {
	public final static String GROUP_ADDR = "230.0.0.1";
	public final static int PORT_NUM = 6000;
	public final static int PACKET_SIZE = 65003;
	public final static int TCP_PORT_NUM = 6100;
	public static int processID;
	protected volatile static String state;
	protected volatile static int count;
	public static Queue<Message> queue = new LinkedList<>();
	public static int N_OF_PROC=1;
	public static volatile ArrayList<ProcessList> proc;
	
	public  static void sendData(String ipAddress,String message){
		try{
			Socket socket = new Socket(ipAddress,Configuration.TCP_PORT_NUM);
			ObjectOutputStream ois = new ObjectOutputStream(socket.getOutputStream());
			ois.writeObject(new Message(message,InetAddress.getLocalHost().getHostName(),Configuration.TCP_PORT_NUM,Configuration.processID));
			ois.close();
			socket.close();
			}catch(UnknownHostException err){
				System.out.println("Couldn't connect to: "+ipAddress);
			} catch(IOException err){
				System.out.println("Couldn't connect to " +ipAddress);
			}
	}
	
}
