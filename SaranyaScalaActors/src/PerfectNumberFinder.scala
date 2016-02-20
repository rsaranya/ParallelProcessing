

/**
 * @author Ron.Coleman
 */
object PerfectNumberFinder {
  def main(args: Array[String]): Unit = {

  }
  def sumOfFactors(number: Int) = {
    (1 to number).foldLeft(0) { (sum, i) => if (number % i == 0) sum + i else sum }
  }

  def isPerfect(candidate: Int) = 2 * candidate == sumOfFactors(candidate)
}