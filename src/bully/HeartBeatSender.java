package bully;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;

public class HeartBeatSender implements Callable<Void> {

	private static InetAddress GROUP_ADDR;
	private HBMessage hbMessage;
	
	
	public HeartBeatSender(String processId) throws UnknownHostException {
		GROUP_ADDR = InetAddress.getByName(Configuration.GROUP_ADDR);
		hbMessage = new HBMessage("HEARTBEAT",InetAddress.getLocalHost().getHostAddress(),Configuration.PORT_NUM,processId);
	}

	public void multicastGroup() throws IOException {

		try (MulticastSocket multi = new MulticastSocket(Configuration.PORT_NUM)) {
			multi.joinGroup(GROUP_ADDR);
			byte[] bytes=serializeBytes(hbMessage);
			DatagramPacket packet = new DatagramPacket(bytes,
					bytes.length, GROUP_ADDR, Configuration.PORT_NUM);
			multi.send(packet);
			
		} finally {

		}
	}

	private byte[] serializeBytes(HBMessage hbMessage) throws IOException {
		byte[] bytes = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(hbMessage);
		oos.flush();
		bytes = bos.toByteArray();
		return bytes;
	}

	

	@Override
	public Void call() throws IOException, InterruptedException {
		while (true) {
			//System.out.println("Starting Heartbeat Sender");
			multicastGroup();
			Thread.sleep(1000);	
		}
	}

}
