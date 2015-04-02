package bully;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Callable;

public class HeartBeatReceiver implements Callable<Void> {

	private static InetAddress groupAddr;

	private volatile String option;

	public HeartBeatReceiver(String option, String processId)
			throws UnknownHostException {
		this.option = option;
		Configuration.proc = new ArrayList<ProcessList>();
		groupAddr = InetAddress.getByName(Configuration.GROUP_ADDR);

	}

	private void updateList() throws InterruptedException, IOException {
		while (true) {
			synchronized (Configuration.proc) {
				long millis = System.currentTimeMillis();
				for (ProcessList list : Configuration.proc) {
					
					if(Integer.parseInt(list.processId) >= Integer.parseInt(Configuration.leader))
						Configuration.leader=list.processId;
					
					if ((millis - list.date.getTime()) > Configuration.HEARTBEAT_TIME) {
						
						if(list.processId.equals(Configuration.leader)){
							Configuration.sendData(InetAddress.getLocalHost().getHostName(), "START");
							
							for(ProcessList list1: Configuration.proc){
								if(Configuration.processID != list1.processId)
									Configuration.sendData(list1.ipAddress, "REMOVE");
							}
						}
						Configuration.proc.remove(list);
						break;
					}
				}
			}
			Thread.sleep(Configuration.CHECK_TIME);
		}
	}

	@Override
	public Void call() throws Exception {
		if (option.equals("RECEIVE")) {
			System.out.println("Starting Heartbeat Receiver");
			receive();
		} else {
			System.out.println("Starting Process List Validator");
			updateList();
		}
		return null;
	}

	private void receive() throws IOException, ClassNotFoundException {
		try (MulticastSocket sock = new MulticastSocket(Configuration.PORT_NUM)) {
			sock.joinGroup(groupAddr);
			System.out.println("Joining Multicast group..");
			while (true) {
				byte[] buff = new byte[Configuration.PACKET_SIZE];
				DatagramPacket pack = new DatagramPacket(buff,
						Configuration.PACKET_SIZE);
				sock.receive(pack);
				//System.out.println("Heart beat received");
				HBMessage message = deserializeBytes(pack.getData());
				updateList(message);
				//display();
			}
		}
	}

	private void display() {
		for (ProcessList pro : Configuration.proc) {
			System.out.println(pro);
		}

	}

	private void updateList(HBMessage message) {

		boolean flag = false;
		synchronized (Configuration.proc) {
			for (ProcessList list : Configuration.proc) {
				if (list.processId.equals(message.processId)) {
					flag = true;
					list.date = new Date();
				}
			}
			if (flag == false) {
				Configuration.proc.add(new ProcessList(message.processId,
						message.ipAddress, message.portNum, new Date()));
			}
		}

	}

	public HBMessage deserializeBytes(byte[] bytes) throws IOException,
			ClassNotFoundException {
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		ObjectInputStream ois = new ObjectInputStream(bis);
		HBMessage message = (HBMessage) ois.readObject();
		return message;
	}

}
