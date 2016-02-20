
/*
 * Name : Saranya Radhakrishnan
 * CWID : 20062589
 * Problem : Parallelize reading of Moby Dick files and get count of every unique words. 
 * Compare the results for uni-processor and multiprocessors
 *
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * This class demonstrates Moby divide-conquer process.
 */
public class MobyDivideConquer implements Runnable {
	public static final int TIMEOUT = 50 * 1000; // used to ensure thread is not
													// waiting for infinite
													// amount of time
	private static int lintIdCounter = 0; // Global id counter for child threads

	// Class variable used for queue of file paths
	private ArrayList<String> larrLstFilePaths = null;
	// Class variable for child dictionary
	private HashMap<String, Double> larrLstWordCount = new HashMap<String, Double>();
	// Class variable used for result queue
	private ArrayList<HashMap<String, Double>> larrLstResult = new ArrayList<HashMap<String, Double>>();

	private String lstrFilePath = ""; // Path of file for this thread
	private int lintChildId = -1; // Contains id for each child thread

	/**
	 * Entry point to the program Contains all the code required for Parent
	 * Thread
	 **/
	public static void main(String[] args) {
		try {
			// Declaring all the local variable

			// Local variable used for queue of file paths
			ArrayList<String> larrLstFilePaths = new ArrayList<>();
			// Local variable used for result queue
			ArrayList<HashMap<String, Double>> larrLstResults = new ArrayList<HashMap<String, Double>>();
			// Local variable for Final parent dictionary
			HashMap<String, Double> larrLstWordCount = new HashMap<String, Double>();
			// Temporary variable for dequeuing from result queue
			HashMap<String, Double> larrLstTempCount = new HashMap<String, Double>();

			// Step 1: Fetch the file paths from the directory in an ArrayList
			final File lFolders = new File("../15-text");

			if (!lFolders.isDirectory()) {
				System.out.println("Path for files does not exist");
				System.exit(0);
			}
			for (File lFile : lFolders.listFiles()) {
				String lstrPath = lFile.getPath();
				larrLstFilePaths.add(lstrPath);
			}

			System.out.println("parent waiting for child...");

			// Set the start time for the entire process
			long llngStartTime = System.currentTimeMillis();
			// Number of cores in the system
			int lintNoOfCores = 1;//Runtime.getRuntime().availableProcessors();

			// Step 2: Launch the child worker threads equivalent to the cores
			Thread child[] = new Thread[lintNoOfCores];
			for (int lintChildId = 0; lintChildId < lintNoOfCores; lintChildId++) {
				MobyDivideConquer lMDCThread = new MobyDivideConquer(larrLstFilePaths, larrLstResults);
				(child[lintChildId] = new Thread(lMDCThread)).start();

			}

			// Step 3: Wait for the child threads to finish processing and push
			// the results
			int lintChildDone = 0;
			while (lintChildDone < lintNoOfCores) {
				{
					synchronized (larrLstResults) {
						try {
							// Wait but not indefinitely
							larrLstResults.wait(TIMEOUT);

							int size = larrLstResults.size();
							// Step 4: Fetch the result HashMap from the queue
							while (larrLstResults.size() > 0) {
								larrLstTempCount = larrLstResults.remove(0);
								for (String key : larrLstTempCount.keySet()) {
									if (larrLstWordCount.containsKey(key))
										larrLstWordCount.put(key,
												larrLstWordCount.get(key) + larrLstTempCount.get(key));
									else
										larrLstWordCount.put(key, larrLstTempCount.get(key));
								}
							}
							lintChildDone += size;
						} catch (Exception ex) {
							System.out.println(ex.getMessage());
						}
					}
				}
			}

			// Contains the end time for processing
			long llngEndTime = System.currentTimeMillis();
			// Total duration of the process
			double ldblWaitTime = (llngEndTime - llngStartTime) / 1000.0;

			// Step 5: Sort the contents of the Hashmap
			ArrayList<WordCount> larrLstSorted = new ArrayList<WordCount>();

			// Convert the Hashmap into a list
			for (String lstrWord : larrLstWordCount.keySet())
				larrLstSorted.add(new WordCount(lstrWord, larrLstWordCount.get(lstrWord)));

			// Sort the list based on the count
			Collections.sort(larrLstSorted);
			int count = 0;
			System.out.println("\nCores, N = " + lintNoOfCores);
			System.out.println("Time Waiting, dT = " + ldblWaitTime);
			double t1 = 0;
			switch (lintNoOfCores) {
			case 1:
				t1 = 0.234;
				break;
			case 2:
				t1 = 0.2;
				break;
			case 4:
				t1 = 0.203;
				break;
			case 12:
				t1 = 0.219;
				break;
			}

			System.out.println("t1 = " + t1);
			System.out.println("R = t1/t"+lintNoOfCores + " = " + t1 / ldblWaitTime);
			System.out.println("e = R/N  = " + (t1 / ldblWaitTime) / lintNoOfCores);

			System.out.println("\n******  TOP 10 WORDS  *****\n");
			for (WordCount lWordCount : larrLstSorted) {
				if (count < 10) {
					System.out.println(lWordCount.lstrWord + "  \t  " + lWordCount.ldblCount);
					count++;
				} else
					break;
			}
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}

	/**
	 * Constructor Maps the parent variables to the child variables to create
	 * shared queues
	 * 
	 * @param parrLstFilePaths
	 *            : Contains the path to every file in the folder
	 * @param parrLstHashResult
	 *            : Contains the hashmap of every child thread
	 */
	public MobyDivideConquer(ArrayList<String> pArrLstFilePaths, ArrayList<HashMap<String, Double>> pArrLstHashResult) {
		try {
			this.lintChildId = lintIdCounter++;
			this.larrLstFilePaths = pArrLstFilePaths;
			this.larrLstResult = pArrLstHashResult;
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	/**
	 * Transfers here when thread started
	 */
	@Override
	public void run() {
		try {
			// Step 1: Loop until all the files have been processed
			while (true) {
				lstrFilePath = "";

				// / ENTERING critical region to safely manipulate the work
				// queue
				synchronized (larrLstFilePaths) {
					// Step 2 : If queue is not empty
					// Dequeue a filename from the queue
					// If no files remaining, run a sleeping thread.
					int lintSize = larrLstFilePaths.size();
					if (lintSize != 0)
						lstrFilePath = larrLstFilePaths.remove(0);
				}
				// / EXITING critical region

				// If no work, we're done, otherwise do work OUTSIDE the
				// critical region
				// as we have our work and there no point risking deadlock.
				// Step 3: Check if File path is not Empty
				if (lstrFilePath.isEmpty())
					break;

				// Step 4 : Process the file for which the path is received
				doWork();
			}

			// Step 5: Push the individual hashmap into the queue and notify the
			// parent thread
			synchronized (larrLstResult) {
				try {
					larrLstResult.add(larrLstWordCount);
					larrLstResult.notify();
				} catch (Exception ex) {
					System.out.println(ex.getMessage());
				}
			}
			System.out.println("Child " + lintChildId + " is done!");
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}

	/**
	 * Process each file to create hashmaps
	 * 
	 */
	private Integer doWork() {
		try {
			FileInputStream lfileInputStreamIn = new FileInputStream(lstrFilePath);
			BufferedReader lbuffReader = new BufferedReader(new InputStreamReader(lfileInputStreamIn));
			String lstrLine = "";

			while ((lstrLine = lbuffReader.readLine()) != null) {
				// Step 2: Split each line into words and insert into an array
				String[] lstrArrWords = lstrLine.toLowerCase().split(" +");

				// Step 3: While inserting into the dictionary, check if the key
				// already exists
				// If exist, update the count
				// Else add the word and count as 1 into the dictionary
				for (int i = 0; i < lstrArrWords.length; i++) {
					if (!lstrArrWords[i].trim().isEmpty()) {
						if (larrLstWordCount.containsKey(lstrArrWords[i]))
							larrLstWordCount.put(lstrArrWords[i], larrLstWordCount.get(lstrArrWords[i]) + 1);
						else
							larrLstWordCount.put(lstrArrWords[i], 1.0);
					}
				}
			}
			lbuffReader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return 0;
	}
}

class WordCount implements Comparable<WordCount> {
	public final String lstrWord;
	public final Double ldblCount;

	public WordCount(String pstrWord, Double pdblCount) {
		this.lstrWord = pstrWord;
		this.ldblCount = pdblCount;
	}

	@Override
	public int compareTo(WordCount p) {
		return (int) (p.ldblCount - this.ldblCount);
	}
}