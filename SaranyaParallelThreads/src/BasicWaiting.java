
import java.util.ArrayList;
import java.util.Arrays;


/**
 * This class demonstrates basic waiting for work to be done.
 * @author Ron.Coleman
 *
 */
public class BasicWaiting implements Runnable {
	
	public static void main(String[] args) {

		ArrayList<Integer> queue = new ArrayList<>(Arrays.asList(1, 3, 4, 5, 12, 2, 7, 9 ));
		
		BasicWaiting runnable = new BasicWaiting(queue);
		
		new Thread(runnable).start();
		
		System.out.println("parent waiting for child...");
		
		long t0 = System.currentTimeMillis();
		
		/// ENTERING critical region to manipulate queue
		synchronized(queue) {
			try {
				queue.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		/// EXITING critical region
		
		long t1 = System.currentTimeMillis();
		
		double totalWaitTime =  (t1 - t0) / 1000.0;
		
		System.out.println("parent detected child done! waiting = "+totalWaitTime);

	}
	
	/** Local reference to shared work queue */
	private ArrayList<Integer> queue = null;
	
	/** Work item for this thread */
	private int workItem = -1;
	
	/**
	 * Constructor
	 * @param queue Work queue
	 */
	public BasicWaiting(ArrayList<Integer> queue) {
		this.queue = queue;
	}

	/**
	 * Transfers here when thread started.
	 */
	@Override
	public void run() {
		// Loop until all work done.
		while (true) {
			workItem = -1;
			
			//// ENTERING critical region to safely manipulate the work queue
			synchronized (queue) {
				int sz = queue.size();

				// If queue NOT empty, get some work and leave the critical region
				if (sz != 0)
					workItem = queue.remove(0);
				else
					// If queue IS empty, notify parent know we've finished.
					queue.notify();
			}
			//// EXITING critical region

			// If no work, we're done, otherwise do work OUTSIDE the critical region
			// as we have our work and there no point risking deadlock.
			if(workItem < 0)
				break;
			
			System.out.println("child working for " + workItem + " seconds");
			
			// TODO: Validate the work result
			doWork();

		}
		
		System.out.println("child done!");

	}
	
	/**
	 * Do some work here and return the results.
	 */
	private Integer doWork() {
		// TODO: validate the work item (e.g., >0)
		try {
			Thread.sleep(workItem * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
			return -1;
		}
		
		return 0;
	}
}

