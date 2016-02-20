/**
 * This class demonstrates synchronizing threads.
 * @author Ron.Coleman
 *
 */
public class BasicSynchronizedThread implements Runnable {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		BasicSynchronizedThread runnable = new BasicSynchronizedThread();
		
		new Thread(runnable).start();
		synchronized(System.out) {
			System.out.println("in parent thread");
		}
	}

	@Override
	public void run() {
		synchronized(System.out) {
			System.out.println("in child thread");
		}
	}

}
