
import java.util.ArrayList;
import java.util.Arrays;


/**
 * This class demonstrates basic divide-conquer process.
 * @author Ron.Coleman
 *
 */
public class BasicDivideConquer implements Runnable {
	public static final int TIMEOUT = 60 * 1000;
	
	public static void main(String[] args) {

		ArrayList<Integer> queue = new ArrayList<>(Arrays.asList(1, 3, 4, 5, 12, 2, 7, 9 ));
		
		System.out.println("parent waiting for child...");
		
		long t0 = System.currentTimeMillis();
		
		int cores = Runtime.getRuntime().availableProcessors();
		
		// Launch the child worker threads
		for(int id=0; id < cores; id++) {
			BasicDivideConquer runnable = new BasicDivideConquer(queue);
			
			new Thread(runnable).start();
		}

		int done = 0;
		
		synchronized(queue) {
			try {
				while(done < cores) {
					// Wait but not indefinitely
					long start = System.currentTimeMillis();
					queue.wait(TIMEOUT);
					long end = System.currentTimeMillis();
					
					// If we've not been waiting too time, everything ok otherwise
					// there may be a problem.
					if((end - start) < TIMEOUT)
						done++;
					else
						break;
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		long t1 = System.currentTimeMillis();
		
		double totalWaitTime =  (t1 - t0) / 1000.0;
		
		if(done == cores)
			System.out.println("parent detected "+done+" children done! waiting = "+totalWaitTime);
		else
			System.out.println((cores-done) + "children did not finish, results uncertain!");
	}
	
	/** Local reference to shared work queue */
	private ArrayList<Integer> queue = null;
	
	/** Work item for this thread */
	private int workItem = -1;
	
	/** Global id counter for child threads */
	private static int idCounter = 0;
	
	/** My id */
	private int id = -1;

	/**
	 * Constructor
	 * @param queue Work queue
	 */
	public BasicDivideConquer(ArrayList<Integer> queue) {
		this.id = idCounter++;
		this.queue = queue;
	}
	
	/**
	 * Transfers here when thread started
	 */
	@Override
	public void run() {
		// Loop until all work done.
		while (true) {
			workItem = -1;
			
			/// ENTERING critical region to safely manipulate the work queue
			synchronized (queue) {
				int sz = queue.size();

				if (sz != 0)
					workItem = queue.remove(0);
				else
					queue.notify();
			}
			/// EXITING critical region

			// If no work, we're done, otherwise do work OUTSIDE the critical region
			// as we have our work and there no point risking deadlock.
			if(workItem < 0)
				break;
			
			System.out.println("child " +id +" working for " + workItem + " seconds");
			
			// TODO: Validate the work result
			doWork();

		}
		
		System.out.println("child "+id+" done!");

	}
	
	/**
	 * Do some work here and return the result.
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


