/**
 * This class demonstrates creating and starting a basic thread using Runnable.
 * @author Ron.Coleman
 *
 */
public class BasicThread implements Runnable {
	public static void main(String[] args) {
		Runnable runnable = new BasicThread();
		
		new Thread(runnable).start();
		
		System.out.println("in parent (main) thread");
	}

	@Override
	public void run() {
		System.out.println("in child thread");
	}
}
