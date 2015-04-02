package bully;

import java.io.IOException;
import java.util.concurrent.Callable;

public class Election implements Callable<Void> {

	private volatile String option;
	private volatile static boolean OK;
	private volatile static boolean COORDINATOR;
	private HBMessage message;

	

	public Election(String option, HBMessage message) {
		this.option = option;
		if(option.equals("ELECTION"))
			Configuration.election=true;
		this.message = message;
		// Configuration.leader =null;
	}

	private void election(HBMessage message) throws IOException,
			InterruptedException {
		System.out.println("Received Election Message from "
				+ message.processId);
		if (Integer.parseInt(message.processId) < Integer
				.parseInt(Configuration.processID)) {

			Configuration.sendData(message.ipAddress, "OK");
			System.out.println("Sending OK message to " + message.processId);
			boolean flag = false;

			for (ProcessList p : Configuration.proc) {
				if (Integer.parseInt(p.processId) > Integer
						.parseInt(Configuration.processID)) {
					flag = true;
					Configuration.sendData(p.ipAddress, "ELECTION");

				}
			}

			if (flag == false) {
				System.out.println("Sending co-ordinator message..");
				COORDINATOR = true;
				for (ProcessList p : Configuration.proc) {
					Configuration.sendData(p.ipAddress, "COORDINATOR");
				}
			} else {

				Thread.sleep(10000/Integer.parseInt(Configuration.processID));
				//System.out.println("Waiting for OK reply..");
				if (OK != true) {
					for (ProcessList p : Configuration.proc) {
						Configuration.sendData(p.ipAddress, "COORDINATOR");
					}
				}

			}

		}

	}

	@Override
	public Void call() throws Exception {
		if (option.equals("ELECTION"))
			election(message);
		else if (option.equals("OK"))
			startTimeOut();
		else if (option.equals("COORDINATOR"))
			setLeader();
		else if(option.equals("REMOVE"))
			removeLeader();
		else {
			startBully();
		}
		return null;
	}

	private void removeLeader() {
		for(ProcessList item : Configuration.proc){
			if(item.processId.equals(Configuration.leader)){
				Configuration.proc.remove(item);
				break;
			}
		}
	}

	private void startBully() throws IOException, InterruptedException {
		
		System.out.println("Starting bully algorithm");

		boolean flag = false;

		for (ProcessList proc : Configuration.proc) {
			if (Integer.parseInt(proc.processId) > Integer
					.parseInt(Configuration.processID)) {
				System.out.println(proc.ipAddress);
				flag = true;
				Configuration.sendData(proc.ipAddress, "ELECTION");
			}
		}

		if (flag == false) {

			
			for (ProcessList p : Configuration.proc) {
				
				Configuration.sendData(p.ipAddress, "COORDINATOR");
			}
		} else {
			Thread.sleep(10000/Integer.parseInt(Configuration.processID));
			if (OK != true){
				
				for (ProcessList p : Configuration.proc) {
					Configuration.sendData(p.ipAddress, "COORDINATOR");
				}
			}
		}
	}

	private void setLeader() {

		System.out.println("Setting the leader to " + message.processId);
		Configuration.leader = message.processId;
		COORDINATOR = true;

	}

	private void startTimeOut() throws IOException {
		try {
			System.out.println("Received OK message from " + message.processId);
			OK = true;
			Thread.sleep(10000/Integer.parseInt(Configuration.processID));
			if (COORDINATOR != true)
				startBully();
		} catch (InterruptedException e) {

		}
	}

}
