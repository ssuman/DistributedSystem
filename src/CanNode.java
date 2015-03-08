import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class CanNode implements Runnable {

	private static String ipAddress;
	private volatile static String VID;
	private static List<Neighbor> neighbor;
	private static int PORT_NUM = 45002;
	// protected final static int RECEIVING_PORT=65535;
	private volatile String message;
	private final int BOOTSTRAP_PORT_NBR = 10000;
	private Message mesg;

	public CanNode(Message msg, String message, String VID, List<Neighbor> neigh)
			throws UnknownHostException {
		this.message = message;
		ipAddress = InetAddress.getLocalHost().getHostAddress();
		this.mesg = msg;
		neighbor = neigh;
		CanNode.VID = VID;
	}

	public void display() {
		System.out.println("");
		System.out.println("Virtual ID: " + VID);
		// System.out.println("Neighbors");
		for (Neighbor n : neighbor) {
			System.out.println("Neighbor Virutal ID: " + n.getVID());
			System.out.println("Neighbor IP address: " + n.getIpAddress());
			System.out.println("Neighbor Port Number: " + n.getPortNo());
		}

		double[][] range = convertVID(VID);
		System.out.println("My Zone info:");
		System.out
				.println("X range: [" + range[0][0] + "," + range[0][1] + "]");
		System.out
				.println("y range: [" + range[1][0] + "," + range[1][1] + "]");

		System.out.println("");
		System.out.println("");
	}

	public CanNode(String message, String VID, List<Neighbor> neigh)
			throws UnknownHostException {
		this.message = message;
		ipAddress = InetAddress.getLocalHost().getHostAddress();
		neighbor = neigh;
		// System.out.println("Inside Cons: " + VID);
		CanNode.VID = VID;
	}

	@Override
	public void run() {
		try {

			identifyRequest(message);

		} catch (IOException err) {
			err.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {

			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized void identifyRequest(String mesge) throws IOException,
			ClassNotFoundException, InterruptedException {

		if (mesge.equals("ROUTE")) {

			route(CanNode.ipAddress, CanNode.PORT_NUM, this.mesg);

		} else if (mesge.equals("JOIN")) {
			startServer();

		} else if (mesge.equals("RECEIVE")) {
			receiveInfo();

		} else if (mesge.equals("UPDATE")) {
			updateVID(mesg.payLoad);

		} else if (mesge.equals("REMOVE")) {
			removeVID(mesg.payLoad, mesg.payLoad.length() - 1);
		} else if (mesge.equals("ADD")) {
			addVID(mesg);
		} else if (mesge.equals("MODIFY")) {
			modify();
		} else if (mesge.equals("LEAVE_UP")) {
			leave_up(mesg);
		} else if (mesge.equals("LEAVE_REMOVE")) {

			removeVID(mesg.payLoad, mesg.payLoad.length());
		} else if (mesge.equals("INFO")) {
			info(mesg);
		} else if (mesge.equals("MODIFY_UP")) {
			modify(mesg, mesg.payLoad.length());
		}

	}

	/***
	 * 
	 * @param mesg2
	 * @param length
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	private void modify(Message mesg2, int length) throws UnknownHostException,
			IOException {
		String newVID = mesg2.payLoad;
		// System.out.println("MODIFY_UP" +newVID);
		for (Neighbor n : neighbor) {
			Socket sock = new Socket(n.getIpAddress(), n.getPortNo());
			ObjectOutputStream ois = new ObjectOutputStream(
					sock.getOutputStream());
			Message msg = new Message("LEAVE_UP", newVID + "," + VID,
					ipAddress, PORT_NUM);
			ois.writeObject(msg);
			sock.close();
		}
		CanNode.VID = newVID;

	}

	private void info(Message mesg) throws UnknownHostException, IOException {

		addMeToList();
		Neighbor n = sameLength();
		// System.out.println("Same Length "+ n.getVID());
		if (n != null) {
			modifyVIDCall(VID, n.getIpAddress(), n.getPortNo());
		}
	}

	private void addMeToList() throws UnknownHostException, IOException {
		for (Neighbor n : neighbor) {
			informNeighbor(n, "ADD");
		}

	}

	private void addVID(Message mesg2) {

		for (Iterator<Neighbor> n = neighbor.iterator(); n.hasNext();) {
			Neighbor curr = n.next();

			if (curr.getVID().equals(mesg2.payLoad)) {
				return;
			}
		}
		neighbor.add(new Neighbor(mesg2.payLoad, mesg2.getIpAdd(), mesg2
				.getPortNo()));
		// display();
	}

	private void modifyVIDCall(String sameLengthVID, String ipAddress, int port)
			throws UnknownHostException, IOException {

		Socket sock = new Socket(ipAddress, port);
		ObjectOutputStream ois = new ObjectOutputStream(sock.getOutputStream());
		Message msg = new Message("MODIFY", sameLengthVID, ipAddress, port);
		ois.writeObject(msg);
		sock.close();
	}

	private void modify() throws UnknownHostException, IOException {
		synchronized (neighbor) {
			String newVID = VID.substring(0, VID.length() - 1);
			for (Iterator<Neighbor> it = neighbor.iterator(); it.hasNext();) {
				Neighbor n = it.next();
				Socket sock = new Socket(n.getIpAddress(), n.getPortNo());
				ObjectOutputStream ois = new ObjectOutputStream(
						sock.getOutputStream());
				Message msg = new Message("LEAVE_UP", newVID + "," + VID,
						ipAddress, PORT_NUM);
				ois.writeObject(msg);
				sock.close();
			}
			CanNode.VID = newVID;
		}

	}

	private Neighbor sameLength() {
		Neighbor N = null;
		for (Neighbor n : neighbor) {
			if (VID.length() == n.getVID().length()) {
				N = n;
			}
		}
		return N;
	}

	private synchronized void leave_up(Message mesg) {
		String splitStr[] = mesg.payLoad.split(",");
		System.out.println("Update " + splitStr[0]);
		System.out.println("Update " + splitStr[1]);
		for (Iterator<Neighbor> n = neighbor.iterator(); n.hasNext();) {
			Neighbor curr = n.next();
			if (curr.getVID().equals(splitStr[1])) {
				curr.setVid(splitStr[0]);
				break;
			}
		}
	}

	private void removeVID(String vid, int length) {
		synchronized (neighbor) {
			for (Iterator<Neighbor> n = neighbor.iterator(); n.hasNext();) {
				Neighbor curr = n.next();
				if (curr.getVID().equals(vid.substring(0, length))) {
					n.remove();
					break;
				}
			}
		}
		// display();
	}

	private synchronized void updateVID(String vid) {

		for (Iterator<Neighbor> n = neighbor.iterator(); n.hasNext();) {

			Neighbor curr = n.next();
			if (curr.getVID().equals(vid.substring(0, vid.length() - 1))) {

				curr.setVid(vid);
				break;
			}
		}
	}

	private void receiveInfo() throws IOException, ClassNotFoundException,
			UnknownHostException, InterruptedException {

		ServerSocket tempServer = new ServerSocket(PORT_NUM);
		Socket temp = tempServer.accept();
		ObjectInputStream ois = new ObjectInputStream(temp.getInputStream());
		List<Neighbor> neigh = (List<Neighbor>) ois.readObject();
		String vid = ois.readUTF();
		String myVid = ois.readUTF();
		String ipAddress = ois.readUTF();
		int portNo = ois.readInt();
		neigh.add(new Neighbor(vid, ipAddress, portNo));
		neighbor = neigh;

		if (myVid == null)
			VID = "1";
		else
			VID = myVid;

		updateNeighbors();
		addMeToList();
		temp.close();
		tempServer.close();
		informBootStrap();
		// display();
		Thread connect = new Thread(new CanNode("JOIN", CanNode.VID, neigh));
		connect.start();
	}

	public void startServer() throws IOException, ClassNotFoundException {

		ServerSocket socket = new ServerSocket(PORT_NUM);
		while (true) {
			try {
				Socket sock = socket.accept();

				ObjectInputStream stream = new ObjectInputStream(
						sock.getInputStream());

				Message msg = (Message) stream.readObject();

				if (!msg.getMessage().equals("INFO")) {

					Thread requestThread = new Thread(new CanNode(msg,
							msg.getMessage(), CanNode.VID, CanNode.neighbor));
					requestThread.start();
				} else {
					// System.out.println("I'm here");
					Thread requestThread = new Thread(new CanNode(msg,
							msg.getMessage(), CanNode.VID, msg.Neighbor));
					requestThread.start();
				}
			} catch (IOException err) {
				err.printStackTrace();
			} catch (Exception err) {
				err.printStackTrace();
			}
			// sock.close();
		}

	}

	public void updateNeighbors() throws UnknownHostException, IOException {
		for (Iterator<Neighbor> n = neighbor.iterator(); n.hasNext();) {
			Neighbor curr = n.next();
			if (!isNeighbor(curr)) {
				informNeighbor(curr, "REMOVE");
				n.remove();
			} else {
				informNeighbor(curr, "UPDATE");
			}

		}
	}

	private void informNeighbor(Neighbor n, String action)
			throws UnknownHostException, IOException {
		Socket sock = new Socket(n.getIpAddress(), n.getPortNo());
		ObjectOutputStream ois = new ObjectOutputStream(sock.getOutputStream());
		// System.out.println("Addding VID "+ VID);
		Message msg = new Message(action, VID, CanNode.ipAddress,
				CanNode.PORT_NUM);
		ois.writeObject(msg);
		ois.flush();
		ois.close();
		sock.close();
	}

	public List<Neighbor> getNeighbor() {
		return neighbor;
	}

	public boolean collinear(Point a, Point b, Point c) {
		return Math
				.abs((a.getXaxis() - c.getXaxis())
						* (b.getYaxis() - c.getYaxis())
						- ((a.getYaxis() - c.getYaxis()) * (b.getXaxis() - c
								.getXaxis()))) == 0;
	}

	public boolean isNeighbor(Neighbor neigh) {

		double neighRange[][] = convertVID(neigh.getVID());
		double currentRange[][] = convertVID(VID);

		Point a = new Point(currentRange[0][0], currentRange[1][0]);
		Point b = new Point(currentRange[0][0], currentRange[1][1]);
		Point c = new Point(neighRange[0][1], neighRange[1][0]);
		Point d = new Point(neighRange[0][1], neighRange[1][1]);

		if (collinear(a, b, c) && collinear(a, b, d))
			return true;

		a = new Point(currentRange[0][0], currentRange[1][0]);
		b = new Point(currentRange[0][1], currentRange[1][0]);
		c = new Point(neighRange[0][0], neighRange[1][1]);
		d = new Point(neighRange[0][1], neighRange[1][1]);
		if (collinear(a, b, c) && collinear(a, b, d))
			return true;

		a = new Point(currentRange[0][0], currentRange[1][1]);
		b = new Point(currentRange[0][1], currentRange[1][1]);
		c = new Point(neighRange[0][0], neighRange[1][0]);
		d = new Point(neighRange[0][1], neighRange[1][0]);
		if (collinear(a, b, c) && collinear(a, b, d))
			return true;

		a = new Point(currentRange[0][1], currentRange[1][0]);
		b = new Point(currentRange[0][1], currentRange[1][1]);
		c = new Point(neighRange[0][0], neighRange[1][0]);
		d = new Point(neighRange[0][0], neighRange[1][1]);
		if (collinear(a, b, c) && collinear(a, b, d))
			return true;

		return false;
	}

	public static int convertBinaryToInt(String binary) {
		int val = 0;
		try {
			val = Integer.valueOf(binary, 2);

		} catch (NumberFormatException e) {
			System.out.println("Number received not in binary format.");
		}
		return val;
	}

	public void join() throws UnknownHostException, IOException,
			ClassNotFoundException, InterruptedException {

		String line = retreiveRandomIP();

		if (line.equals("")) {
			CanNode.VID = "";
			neighbor = new ArrayList<>();
			informBootStrap();
			// display();
			Thread thread = new Thread(this);
			thread.start();

		} else {
			String ipAddrPort[] = line.split(",");
			if (ipAddrPort != null) {
				Thread thread = new Thread(new CanNode("RECEIVE", "",
						new ArrayList<Neighbor>()));
				thread.start();
				// System.out.println(ipAddrPort[0]+" "+Integer.parseInt(ipAddrPort[1]));
				route(ipAddrPort[0], Integer.parseInt(ipAddrPort[1]), mesg);

			}
		}
	}

	/***
	 * Check entry point has the point. Else find its nearest neighbor to the
	 * point And route the message to that node.
	 * 
	 * @param ipAddress
	 * @param portNum
	 * @param point
	 * @return
	 * @throws IOException
	 * @throws UnknownHostException
	 */
	public synchronized void route(String ipAddress, int PortNum, Message mesg)
			throws UnknownHostException, IOException {

		int portNo = PortNum;
		if (!message.equals("JOIN")) {
			double[][] ranges = convertVID(CanNode.VID);
			List<Neighbor> neigh = getNeighbor();
			if (pointExists(mesg.getPoint(), ranges)) {

				Socket nodeJoin = new Socket(mesg.getIpAdd(), mesg.getPortNo());
				ObjectOutputStream out = new ObjectOutputStream(
						nodeJoin.getOutputStream());

				String temp = null;
				if (VID == null) {
					VID = "0";
					temp = "1";
				} else {
					temp = VID + "1";
					VID = VID + "0";
				}

				List<Neighbor> tempNeigh = neighbor;
				neighbor.add(new Neighbor(temp, mesg.getIpAdd(), mesg
						.getPortNo()));
				updateNeighbors();
				out.writeObject(tempNeigh);
				out.writeUTF(VID);
				out.writeUTF(temp);
				out.writeUTF(CanNode.ipAddress);
				out.writeInt(CanNode.PORT_NUM);
				out.flush();
				nodeJoin.close();
				// display();
				return;
			}

			// System.out.println(mesg.getPoint());
			// display();
			Neighbor nearNeigh = smallestDistance(neigh, mesg.getPoint());
			// System.out.println(nearNeigh.getVID());
			portNo = nearNeigh.getPortNo();
			ipAddress = nearNeigh.getIpAddress();

			// System.out.println(portNo + " " + ipAddress);
		}

		Socket sock = new Socket(ipAddress, portNo);
		ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
		mesg.setMessage("ROUTE");
		out.writeObject(mesg);
		sock.close();
	}

	private void informBootStrap() throws UnknownHostException, IOException {
		Socket newCon = new Socket("glados.cs.rit.edu", BOOTSTRAP_PORT_NBR);
		Writer newOut = new PrintWriter(newCon.getOutputStream());
		newOut.write("ADD:" + ipAddress + "," + String.valueOf(PORT_NUM)
				+ "\r\n");
		newOut.flush();
		newCon.close();
	}

	public static double[][] convertVID(String vid) {

		if (vid == null)
			return new double[][] { { 0.0, 1.0 }, { 0.0, 1.0 } };
		Point[] p = new Point[4];
		for (int i = 0; i < 4; i++) {
			p[i] = new Point();
		}

		// This will provide us the x range
		double x[] = { 0, 1 };
		for (int i = 0; i < vid.length(); i = i + 2) {
			if (vid.charAt(i) == '0') {
				double avg = (x[0] + x[1]) / 2;
				x[1] = avg;
			} else {
				double avg = (x[0] + x[1]) / 2;
				x[0] = avg;
			}
		}

		// This will provide us the y range.
		double y[] = { 0, 1 };
		for (int i = 1; i < vid.length(); i = i + 2) {
			if (vid.charAt(i) == '0') {
				double avg = (y[0] + y[1]) / 2;
				y[1] = avg;
			} else {
				double avg = (y[0] + y[1]) / 2;
				y[0] = avg;
			}
		}

		double[][] ranges = { { x[0], x[1] }, { y[0], y[1] } };
		return ranges;
	}

	private String retreiveRandomIP() throws UnknownHostException, IOException,
			SocketException {

		Socket sock = new Socket("glados.cs.rit.edu", BOOTSTRAP_PORT_NBR);
		Writer out = new PrintWriter(sock.getOutputStream());
		String message = "JOIN" + "\r\n";
		out.write(message);
		out.flush();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				sock.getInputStream()));
		String line = reader.readLine();
		sock.close();
		return line;

	}

	private boolean pointExists(Point point, double[][] ranges) {
		return point.getXaxis() >= ranges[0][0]
				&& point.getXaxis() <= ranges[0][1]
				&& point.getYaxis() >= ranges[1][0]
				&& point.getYaxis() <= ranges[1][1];
	}

	public void leave() throws UnknownHostException, IOException {
		String sameLengthVID = null;
		String ipAddress = null;
		int port = 0;
		for (Neighbor n : neighbor) {
			if (n.getVID().length() == VID.length()) {
				sameLengthVID = n.getVID();
				ipAddress = n.getIpAddress();
				port = n.getPortNo();
			}
		}
		if (sameLengthVID != null) {
			modifyVIDCall(sameLengthVID, ipAddress, port);
			leaveRemove(ipAddress);
			return;
		}

		// Take the highest Neighbor VID value
		// remove from that neighbor from the list
		Neighbor N = takeOverNode();
		neighbor.remove(N);

		// Pass the leaving node neighbor list to take-over neighbor
		Socket sock = new Socket(N.getIpAddress(), N.getPortNo());
		ObjectOutputStream ois = new ObjectOutputStream(sock.getOutputStream());
		Message msg = new Message("INFO", VID, CanNode.ipAddress, PORT_NUM,
				neighbor);
		ois.writeObject(msg);
		sock.close();

		// remove from neighbors
		leaveRemove(ipAddress);

		Socket sock1 = new Socket(N.getIpAddress(), N.getPortNo());
		ObjectOutputStream ois1 = new ObjectOutputStream(
				sock1.getOutputStream());
		Message msg1 = new Message("MODIFY_UP", VID, CanNode.ipAddress,
				PORT_NUM, neighbor);
		ois1.writeObject(msg1);
		sock1.close();

	}

	private void leaveRemove(String ipAddress) throws UnknownHostException,
			IOException {
		for (Neighbor n : neighbor) {
			Socket removeSock = new Socket(n.getIpAddress(), n.getPortNo());
			ObjectOutputStream os = new ObjectOutputStream(
					removeSock.getOutputStream());
			Message removeMessage = new Message("LEAVE_REMOVE", VID, ipAddress,
					PORT_NUM);
			os.writeObject(removeMessage);
			removeSock.close();
		}
	}

	private Neighbor takeOverNode() {
		int max = Integer.MIN_VALUE;
		Neighbor takeOverN = null;
		for (Neighbor n : neighbor) {
			int temp = convertBinaryToInt(n.getVID());
			if (temp >= max) {
				max = temp;
				takeOverN = n;
			}
		}
		return takeOverN;
	}

	public Neighbor smallestDistance(List<Neighbor> neigh, Point point) {

		double temp = 99999999;
		int index = 0, i = 0;
		for (Neighbor n : neigh) {
			double[][] range = convertVID(n.getVID());
			double x_mid = (range[0][0] + range[0][1]) / 2;
			double y_mid = (range[1][0] + range[1][1]) / 2;
			double dist = Point.calculateDistance(new Point(x_mid, y_mid),
					point);
			// System.out.println(dist +" "+n.getVID());

			if (dist <= temp) {
				index = i;
			}
			temp = dist;
			i++;
		}
		return neigh.get(index);
	}

	public static void main(String args[]) throws UnknownHostException,
			IOException, InterruptedException, ClassNotFoundException {

		/*
		 * CanNode node = new CanNode("JOIN","",new ArrayList<Neighbor>());
		 * List<Neighbor> neighbor = new ArrayList<>(); neighbor.add(new
		 * Neighbor("11")); neighbor.add(new Neighbor("0")); Point point = new
		 * Point(0.4429617695950192,0.2596953917760547); Neighbor n
		 * =node.smallestDistance(neighbor, point);
		 * System.out.println(n.getVID());
		 */
		Random random = new Random();
		double xaxis = random.nextDouble();
		double yaxis = random.nextDouble();
		Point point = new Point(xaxis, yaxis);

		CanNode node = new CanNode("JOIN", "", new ArrayList<Neighbor>());
		// node.setPortNo(Integer.parseInt(portNum));
		Message msg = new Message(point, "JOIN", node.getIpAddress(),
				node.getPortNum());
		node.setMessage(msg);
		node.join();

		int ch;
		while (true) {
			System.out.println("1. Display");
			System.out.println("2. Leave");

			Scanner sc = new Scanner(System.in);
			ch = sc.nextInt();
			switch (ch) {
			case 1:
				node.display();
				break;
			case 2:
				node.leave();
				System.out.println("Bye bye");
				System.exit(0);
				break;
			default:
				System.out.println("Try again !");
			}

		}

	}

	public void setMessage(Message msg) {
		this.mesg = msg;
	}

	public void setPortNo(int p) {
		CanNode.PORT_NUM = p;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public int getPortNum() {
		return PORT_NUM;
	}
}
