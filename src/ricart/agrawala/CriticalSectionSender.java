package ricart.agrawala;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;

public class CriticalSectionSender implements Callable<Void> {

	public CriticalSectionSender() {
		System.out.println("State set to RELEASED");
		Configuration.state = "RELEASED";
		Configuration.count = 0;
	}

	@Override
	public Void call() {
		try {
			enterRicart();
			
		} catch (IOException e) {

			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

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

	private void enterRicart() throws IOException, InterruptedException {
		Configuration.state = "WANTED";
		Thread.sleep(5000);
		System.out.println("Changing state to " +Configuration.state);
		multiCastSocket();
		while (true) {
			
			System.out.println(Configuration.count+" "+Configuration.N_OF_PROC);
			if (Configuration.count == Configuration.N_OF_PROC) {
				break;
			}
			Thread.sleep(1000);
			
		}
		Configuration.state = "HELD";
		System.out.println("Changing state to " +Configuration.state);
		Thread.sleep(15000);
		Configuration.state = "RELEASED";
		System.out.println("Changing state to "+Configuration.state);
		
		if (Configuration.queue.size() != 0) {
			int count = Configuration.queue.size();
			for (int i = 0; i < count; i++) {
				synchronized (Configuration.queue) {
					Message message = Configuration.queue.remove();
					Configuration.sendData(message.ipAddress, "OK");
					System.out.println("Sending OK message to items in queue "+message.processId);
				}
			}
		}
		
	}

	private void multiCastSocket() throws IOException, UnknownHostException {

		try (MulticastSocket multi = new MulticastSocket(Configuration.PORT_NUM)) {
			multi.joinGroup(InetAddress.getByName(Configuration.GROUP_ADDR));
			Message message = new Message("CS", InetAddress.getLocalHost()
					.getHostName(), Configuration.PORT_NUM,
					Configuration.processID);

			byte[] bytes = serializeBytes(message);
			DatagramPacket packet = new DatagramPacket(bytes, bytes.length,
					InetAddress.getByName(Configuration.GROUP_ADDR),
					Configuration.PORT_NUM);
			multi.send(packet);

		} finally {

		}
	}

}
