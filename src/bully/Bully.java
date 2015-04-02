package bully;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Bully implements Callable<Void> {

	public static void main(String[] args) throws UnknownHostException {
		ExecutorService service = Executors.newFixedThreadPool(4);
		Configuration.processID = args[0];
		Configuration.leader=args[0];
		Future<Void> future = service.submit(new HeartBeatSender(args[0]));
		Future<Void> rFuture = service.submit(new HeartBeatReceiver("RECEIVE",
				args[0]));
		Future<Void> vFuture = service.submit(new HeartBeatReceiver("VALIDATE",
				args[0]));
		Future<Void> bullyFuture = service.submit(new Bully());

		
		service.shutdown();
		
	}

	@Override
	public Void call() throws Exception {
		System.out.println("Starting TCP Server");
		try (ServerSocket serverSocket = new ServerSocket(
				Configuration.TCP_PORT_NUM)) {
			while (true) {
				Socket socket = serverSocket.accept();
				ObjectInputStream ois = new ObjectInputStream(
						socket.getInputStream());
				HBMessage message = (HBMessage) ois.readObject();
				
				decide(message);
				socket.close();
			}
		}
	}

	private void decide(HBMessage message) throws IOException, InterruptedException {
		ExecutorService service = Executors.newFixedThreadPool(3);
		Future<Void> future = service.submit(new Election(message.content,message));
		service.shutdown();
	}

}
