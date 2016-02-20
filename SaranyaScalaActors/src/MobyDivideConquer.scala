
/**
 * Name : Saranya Radhakrishnan
 * CWID : 20062589
 * Problem : Parallelize reading of Moby Dick files and get count of top 10 unique words. 
 * Compare the results for uni-processor and multiprocessors.
 * Also Compare the results with java code
 *
 */

/** Import java packages */
import java.lang;
import java.io._;

/** Import scala packages */
import scala.collection.mutable._;
import scala.actors.Actor._;
import scala.io._

/**
 * This object demonstrates Moby divide-conquer process in Scala.
 */
object MobyDivideConquer {

      /**
       * Entry point to the program 
       * Contains all the code required for Parent Thread
       */
    	def main(args : Array[String]) : Unit ={
       try{
    			    val TIMEOUT = 50000
    
              //Start time for the first loop
              val lintStartTime : Double = System.currentTimeMillis()
              
    					//Step 1 : Get the number of processors
    					val lintNoOfCores = Runtime.getRuntime.availableProcessors
    
    					//Step 2 : Read Directory of files into a list
    					val lstrFileNames : List[String] = getFiles("../moby-38205/")//("./Data/");
    
              //Check if the directory is not incorrect
              if(lstrFileNames == null)
              {
                println("Incorrect directory. Please enter the correct directory")
                exit()
              } 
              
    			    //Total number of files for processing
    			    val lintTotalLength = lstrFileNames.length
    					// Used to receive message from the child actors
    					val caller = self
    					//Used to increment the range
    					val lintDifference = (lintTotalLength.toDouble/lintNoOfCores).toDouble.ceil.toInt
    
              
    					//Step 3 : Parallelize the code for number of cores
    					(0 to lintNoOfCores - 1).foreach { i => 
    					      // Lower limit of the file to be processed by the child actor 
    					      val lower = i * lintDifference 
    
    					      // Upper limit of the file to be processed by the child actor
    					      val upper = (lintTotalLength - 1) min (( i + 1 ) * lintDifference - 1)
    					      
                    //Spawn the child actor
    					      actor {
    						        caller ! processFiles(lstrFileNames,lower, upper)
    					      }
    					} 
    
    					//Step 4: Process the output
    					processOutput(lintNoOfCores, "Core", lintStartTime, lintNoOfCores, lintTotalLength)
              
              if(lintTotalLength < 200 )
              {
                      //Start time for the second loop
            					val lintSecondStartTime : Double = System.currentTimeMillis()
            
            					//Step 5 : Parallelize the code for number of files
            					(0 until lintTotalLength).foreach { i  =>
            					        // Lower limit of the file to be processed by the child actor 
            					        val lower = i  
            					        
                              //Spawn the child actor
            					        actor {
            						         caller ! processFiles(lstrFileNames,lower, lower)
            					        }
            					} 
            
            					//Step 6 : Process the output
            					processOutput(lintTotalLength, "File", lintSecondStartTime, lintNoOfCores, lintTotalLength) 
              }
       }
       catch{
           case e : Exception => println(e.getMessage)
       }
    	}

      
      /**
       * Receives the result from each child actor
       * And merges all the partial Hash maps into a final hash map.
       * It also displays all the output parameters.
       * 
       * @param pintLimitForLoop
       *            : Contains the number of expected results for which the parent is to wait.
       *            
       * @param pstrTypeOfLoop
       *            : Contains the lower index from which the files are to be processed.
       *  
       * @param pintStartTime
       *            : Contains the time at which the entire file processing started.
       *  
       *  @return pintNumOfCores
       *            : Contains the number of cores utilized for processing the files.
       */
    	def processOutput(pintLimitForLoop : Int, pstrTypeOfLoop : String, pintStartTime : Double , pintNumOfCores : Int, pintTotalLength : Int) : Unit ={
    			try{
        				//Step 4 and 6 : Receive partial Hashmaps from child actors
        				val lhashParentResult  = (0 until pintLimitForLoop ).foldLeft(HashMap[String, Int]()) {
                      (lhashPartialResult, lintIndex) => {
                    					receive {
                    						    //Receive message of type Hashmap
                          					case lobjhashReceived : HashMap[_, _] => {
                            						val lhashReceived = lobjhashReceived.asInstanceOf[HashMap[String, Int]]
                            								for(lkeyValuePair <- lhashReceived){
                            									    val count = lhashPartialResult.getOrElse(lkeyValuePair._1,0)
                            											lhashPartialResult(lkeyValuePair._1) = count + lhashReceived(lkeyValuePair._1)
                            								}
                            						lhashPartialResult
                          					}
                                    case null => {
                                         println("Child "+ lintIndex +"'s Hashmap received is null")
                                         null
                                    }
                    				  }
            				 }
        				}
    
        				// End time for both type of processes : File and Core
        				val lintEndTime  : Double = System.currentTimeMillis()
    						// Total Wait time for both type of processes : File and Core
    						val lintWaitTime : Double = (lintEndTime - pintStartTime)/ 1000.0; 
    
        				//Check if the parent hashmap is not null
                if(lhashParentResult == null)
                {
                    println("Result Hashmap is null.")
                    exit()
                }
                
                //Step 7 : Sort the final hashmap
    				    val llstSortedHash = lhashParentResult.toSeq.sortBy(_._2).reverse.toList
    
    						println("\n============ OUTPUT (for "+ pstrTypeOfLoop +") ============= ") 
    
    						//Step 8: Display top 10 results
    						for(count <- 0  until  10)
    							println(llstSortedHash(count)._1 + "\t"+ llstSortedHash(count)._2)
    
    
  							//Step 9 : Display the parameters like Efficiency , Speedup , etc..
  							println("\nNumber of cores, N : "+ pintNumOfCores);
  						  println("Wait Time, dt :  "+ lintWaitTime);
  
  							val lintTimeForT1 = pintNumOfCores match {
                                          								case 1 => {
                                          									lintWaitTime
                                          								} 
    
                                          								case 4 =>   {
                                          									pstrTypeOfLoop match {
                                                    									case "Core" => {
                                                                                         if( pintTotalLength == 13635)
                                                                                           16.143
                                                                                         else if(  pintTotalLength == 38205 )
                                                                                           155.907
                                                                                         else
                                                                                           1.952
                                                                                      }  
                                                    									case "File" => 0.398
                                          									}
                                          								} 
                                                          
                                          								case 8 =>   {
                                          									pstrTypeOfLoop match {
                                          									case "Core" => 0.372
                                          									case "File" => 0.334
                                          									}
                                          								} 
    								                                      
                                                          case _ =>  
                                                            println("Unexpected number of cores!!")
                                                            1
                }
    
								println("Wait Time for 1 core, T1 :" + lintTimeForT1)

								if( lintWaitTime == 0 ) 
								{
  									val lintR = lintTimeForT1 / 1
  									println("R = t1/t"+pintNumOfCores + " = "+ lintR);
  									println("Efficiency, e: "+ lintR/pintNumOfCores);
								}
								else
								{
  									val lintR = lintTimeForT1 / lintWaitTime
  									println("Speedup, R :"+ lintR);
  									println("Efficiency, e = R/N  = " + lintR/pintNumOfCores);
								}
    			}
          catch{
    			    case e : Exception => println(e.getMessage)
    			}
    	}


      /**
       * Process each file in the range of Lower to Upper in the List of Files 
       * It creates a dictionary or Hashmap of all unique words and their count 
       * as found in the files.
       * It also returns this hashmap back to the calling function.
       * 
       * @param plstFileName
       *            : Contains the list of file paths.
       *            
       * @param pintLower
       *            : Contains the lower index from which the files are to be processed.
       *  
       * @param pintUpper
       *            : Contains the upper index upto which the files are to be processed.
       *  
       *  @return lhashChildResult
       *            : Contains the hashmap of all unique words and their count 
       *              from all the files processed by the actor.
       */
    	def processFiles(plstFileName : List[String], pintLower: Int, pintUpper: Int): HashMap[String, Int] ={
        try{
        			//Declare a local Hashmap for the actor
        			val lhashChildResult : HashMap[String, Int] = new HashMap[String, Int]()
        
        					//For every file in the range Lower -> Upper
        					(pintLower to pintUpper).foreach{ lintFileIndex => {
        						// Step1 : Read the file content line by line
        						val llstLines = Source.fromFile(plstFileName(lintFileIndex)).getLines.toList
        
        								//Step2: for every line in the Lines array -> split it into words
        								llstLines.foreach{ lstrLine =>
        								val llstWords = lstrLine.trim.toLowerCase.split(" +").toList
        
        								// Step3: For every word, check if it exists in the hashmap
        								//if exists, increase the counter
        								// else insert into the hashmap
        								llstWords.foreach{ lstrWord => {
        									if(lstrWord.trim != "")
        									{
        										val count = lhashChildResult.getOrElse(lstrWord.trim.toLowerCase, 0)
        												lhashChildResult(lstrWord.trim.toLowerCase) = count + 1
        									}
        								}
        								}
        						}
        					}
        	}
        	lhashChildResult
        }
        catch{
             case e : Exception => println(e.getMessage) 
             null
        }
    	}
    
      /**
       * Fetches the file paths for all files from a given directory 
       * And returns it to the calling function
       * 
       * @param pstrDirectory
       *            : Contains the parent directory path.
       *  
       *  @return 
       *            : Contains a List of file paths
       */
    	def getFiles(pstrDirectory: String): List[String] =  {
        try{
              //Fetch the files from the given directory.
			        val lfileRootDir = new File(pstrDirectory) 
              //Save the file paths in a list and return it to the calling function
			        lfileRootDir.listFiles.filter(_.isFile).map { lfile=> lfile.getPath }.toList
          }
          catch{
               case e : Exception => println(e.getMessage) 
               null
          }
	    }
}