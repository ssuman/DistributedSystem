package ricart.agrawala;

public class CSReceiverHandler implements Runnable{

	private Message message;
	
	
	public CSReceiverHandler(Message message){
		this.message=message;
	}
	
	@Override
	public void run() {
		checkHeld();
		
	}

	private void checkHeld() {
		if(Configuration.state.equals("HELD")
				|| (Configuration.state.equals("WANTED"))){
			synchronized(Configuration.queue){
				System.out.println("Queuing the request: "+ message.processId);
				Configuration.queue.add(message);
			}
			
		}
		else{
			if(Configuration.processID != message.processId)
			 System.out.println("Not entering Critical section, replying to " + message.processId);
			Configuration.sendData(message.ipAddress, "OK");
		}
		
	}

}
