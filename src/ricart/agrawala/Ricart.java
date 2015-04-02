package ricart.agrawala;

import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Ricart {

	public static void main(String[] args) throws UnknownHostException, InterruptedException, ExecutionException {

		
		ExecutorService service = Executors.newFixedThreadPool(5);
		Configuration.processID=Integer.parseInt(args[0]);
		service.execute(new CriticalSectionReceiver());
		service.execute(new CSServerSocket());
		service.execute(new HeartBeatReceiver());
		service.execute(new HeartBeatSender(Configuration.processID));
		critical();
		
		service.shutdown();

	}

	private static void critical() throws InterruptedException, ExecutionException {
		while (true) {
			
			System.out.println("1. Critical Section");
			System.out.println("2. Exit");
			
			Scanner sc = new Scanner(System.in);
			int decision = Integer.parseInt(sc.nextLine());

			switch (decision) {
				case 1:
					ExecutorService service = Executors.newFixedThreadPool(3);
					Future<Void> future =service.submit(new CriticalSectionSender());
					future.get();
					service.shutdown();
					break;
				case 2:
					sc.close();
					return;
			}
			
		}

	}

}
