

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class BootStrap implements Runnable {

	//Logger logger = Logger.getLogger(this.getClass().getName());
	ServerSocket serverSocket;
	List<String> ipAddressMap;
	private static Socket clientSocket;

	/***
	 * BootStrap Constructor.
	 * 
	 * @param serverSocket
	 * @param ipMap
	 */
	public BootStrap(ServerSocket serverSocket, List<String> ipMap) {

	
		this.serverSocket = serverSocket;
		this.ipAddressMap = ipMap;
	}

	/***
	 * @return A randomly chosen IP address will be returned
	 */
	public String getCANNode() {
		//logger.info("Retreiving an IP address");
		int nbrOfIp = ipAddressMap.size();
		if(nbrOfIp==0)
			return "";
		Random rdm = new Random();
		int index = Math.abs(rdm.nextInt() % nbrOfIp);
		return ipAddressMap.get(index);
	}

	/***
	 * Keep the ipAddressList current
	 * 
	 * @param ipAddress
	 *            ipAddress which needs to be added to the system
	 * @return
	 */
	public boolean updateList(String ipAddress) {
		//logger.info("Adding new IP address");
		return ipAddressMap.add(ipAddress);
	}

	/***
	 * Main program starts here.
	 * 
	 */
	public static void main(String[] args) throws IOException {

		List<String> ipAddressList = new ArrayList<>();

		int PORT_NO = Integer.parseInt(args[0]);

		ServerSocket serverSocket = new ServerSocket(PORT_NO);
		serverSocket.setReuseAddress(true);
		BootStrap bootStrap = new BootStrap(serverSocket, ipAddressList);
	
		try {
			while (true) {
				clientSocket = serverSocket.accept();
				Thread newThread = new Thread(bootStrap);
				newThread.start();

			}
		} catch (Exception err) {

		}

	}

	/***
	 * Execution for thread starts here. Accepts the server connection Obtains
	 * an IP Address returns it to the new node.
	 */
	@Override
	public void run() {
		try {

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					clientSocket.getInputStream()));
			Writer output = new PrintWriter(new OutputStreamWriter(
					clientSocket.getOutputStream()));
			String include = reader.readLine();
			if (include.equals("JOIN")) {
				
				String ipAddress = getCANNode();
				output.write(ipAddress + "\n");
				output.flush();
			}
			else{
				
				String[] ipAdd = include.split(":");
				//logger.info(ipAdd[1]);
				updateList(ipAdd[1]);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
