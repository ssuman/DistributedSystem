package ricart.agrawala;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;

public class HeartBeatReceiver implements Runnable {

	private static InetAddress groupAddr;

	public HeartBeatReceiver()
			throws UnknownHostException {
		
		Configuration.proc = new ArrayList<ProcessList>();
		groupAddr = InetAddress.getByName(Configuration.GROUP_ADDR);

	}

	@Override
	public void run(){
		try {
			receive();
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private void receive() throws IOException, ClassNotFoundException {
		try (MulticastSocket sock = new MulticastSocket(65001)) {
			sock.joinGroup(groupAddr);
			System.out.println("Joining Multicast group..");
			while (true) {
				byte[] buff = new byte[Configuration.PACKET_SIZE];
				DatagramPacket pack = new DatagramPacket(buff,
						Configuration.PACKET_SIZE);
				sock.receive(pack);
				//System.out.println("Heart beat received");
				Message message = deserializeBytes(pack.getData());
				updateList(message);
				//display();
			}
		}
	}

	private void updateList(Message message) {

		boolean flag = false;
		synchronized (Configuration.proc) {
			for (ProcessList list : Configuration.proc) {
				if (list.processId == message.processId) {
					flag = true;
					list.date = new Date();
				}
			}
			if (flag == false) {
				Configuration.proc.add(new ProcessList(message.processId,
						message.ipAddress, message.portNum, new Date()));
				System.out.println(message.processId);
				Configuration.N_OF_PROC++;
			}
		}

	}

	public Message deserializeBytes(byte[] bytes) throws IOException,
			ClassNotFoundException {
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		ObjectInputStream ois = new ObjectInputStream(bis);
		Message message = (Message) ois.readObject();
		return message;
	}

}
