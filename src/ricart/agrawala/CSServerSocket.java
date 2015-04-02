package ricart.agrawala;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class CSServerSocket implements Runnable {

	@Override
	public void run() {
		try {
			startServer();
		} catch (IOException e) {

			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	}

	private void startServer() throws IOException, ClassNotFoundException {
		try (ServerSocket serverSocket = new ServerSocket(
				Configuration.TCP_PORT_NUM)) {
			while (true) {
				Socket socket = serverSocket.accept();
				ObjectInputStream ois = new ObjectInputStream(
						socket.getInputStream());
				Message message = (Message) ois.readObject();
				System.out.println("Received reply message from "+message.processId);
				if (message.content.equals("OK")) {
					Configuration.count += 1;
				}
				System.out.println("Total Number of replies received: "+Configuration.count);
			}
		}

	}

}
