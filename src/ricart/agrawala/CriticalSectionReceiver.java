package ricart.agrawala;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CriticalSectionReceiver implements Runnable {

	public CriticalSectionReceiver(){
		Configuration.state="RELEASED";
		
	}
	@Override
	public void run() {
		try {
			System.out.println("Starting the critical section receiver.");
			receiptRicart();
		} catch (IOException e) {

			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void receiptRicart() throws IOException, ClassNotFoundException {
		try (MulticastSocket socket = new MulticastSocket(
				Configuration.PORT_NUM)) {
			socket.joinGroup(InetAddress.getByName(Configuration.GROUP_ADDR));
			while (true) {
				byte[] buff = new byte[Configuration.PACKET_SIZE];
				DatagramPacket packet = new DatagramPacket(buff,
						Configuration.PACKET_SIZE);
				socket.receive(packet);
				Message message = deserializeBytes(packet.getData());
				System.out.println("Critical section request message received from "+message.processId);
				startThread(message);
			}

		}

	}

	private void startThread(Message message) {
		ExecutorService service = Executors.newFixedThreadPool(2);
		service.execute(new CSReceiverHandler(message));
		
	}

	public Message deserializeBytes(byte[] bytes) throws IOException,
			ClassNotFoundException {
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		ObjectInputStream ois = new ObjectInputStream(bis);
		Message message = (Message) ois.readObject();
		return message;
	}

}
