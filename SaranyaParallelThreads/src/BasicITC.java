import java.util.ArrayList;
import java.util.Arrays;


/**
 * This class demonstrates basic ITC. However, it has a flaw. Can you find it?
 * Run the code to see it.
 * @author Ron.Coleman
 *
 */
public class BasicITC implements Runnable {
	
	
	public static void main(String[] args) {
		ArrayList<Integer> queue = new ArrayList<>(Arrays.asList(1, 3, 4, 5, 12, 2, 7, 9 ));
		
		BasicITC runnable = new BasicITC(queue);
		
		new Thread(runnable).start();
		
//		// This leads to a deadlock.
//		synchronized(queue) {
//			while(!queue.isEmpty());
//		}

		// Polling here is better but it is inefficient
		System.out.println("parent waiting for child...");
		
		Boolean empty = false;
		
		while(!empty) {
			synchronized(queue) {
				empty = queue.isEmpty();
			}
		}
		
		System.out.println("parent detected child done!");

	}
	
	/** Local reference to shared work queue */
	private ArrayList<Integer> queue = null;
	
	/** Work item for the thread */
	private int workItem = -1;
	
	/**
	 * Constructor
	 * @param queue Work queue
	 */
	public BasicITC(ArrayList<Integer> queue) {
		this.queue = queue;
	}

	/** Transfers here when thread started. */
	@Override
	public void run() {
		while (true) {
			workItem = -1;
			
			/// ENTERING critical region to manipulate the work queue
			synchronized (queue) {
				int sz = queue.size();

				if (sz != 0)
					workItem = queue.remove(0);
			}
			/// EXITING critical region

			// If no work, we're done, otherwise do work OUTSIDE the critical region
			// as we have our work and there no point risking deadlock.
			if(workItem < 0)
				break;
			
			System.out.println("child working for " + workItem + " seconds");
			
			// TODO: validate the result
			doWork();

		}
		
		System.out.println("child done!");
	}
	
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
