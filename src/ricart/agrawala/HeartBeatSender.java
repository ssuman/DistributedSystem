package ricart.agrawala;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public class HeartBeatSender implements Runnable{

	private static InetAddress GROUP_ADDR;
	private Message hbMessage;
	
	
	public HeartBeatSender(int processId) throws UnknownHostException {
		GROUP_ADDR = InetAddress.getByName(Configuration.GROUP_ADDR);
		hbMessage = new Message("HEARTBEAT",InetAddress.getLocalHost().getHostAddress(),65001,processId);
	}

	public void multicastGroup() throws IOException {

		try (MulticastSocket multi = new MulticastSocket(65001)) {
			multi.joinGroup(GROUP_ADDR);
			byte[] bytes=serializeBytes(hbMessage);
			DatagramPacket packet = new DatagramPacket(bytes,
					bytes.length, GROUP_ADDR, 65001);
			multi.send(packet);
			
		} finally {

		}
	}

	private byte[] serializeBytes(Message hbMessage) throws IOException {
		byte[] bytes = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(hbMessage);
		oos.flush();
		bytes = bos.toByteArray();
		return bytes;
	}

	

	@Override
	public void run(){
		while (true) {
			//System.out.println("Starting Heartbeat Sender");
			try {
				multicastGroup();
				Thread.sleep(1000);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				
		}
	}

}
