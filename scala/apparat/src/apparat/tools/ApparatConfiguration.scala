package apparat.tools

import scala.collection.mutable.HashMap

/*object ApparatConfiguration {
  def fromArgs(args: Array[String]) = {
    val cfg = new ApparatConfiguration
    cfg parse args
    cfg
  }
}*/

class ApparatConfiguration {
  private val options = new HashMap[String, String]

  def apply(key: String) = options.get(key)
  def update(key: String, value: String) = options(key) = value
  
  def parse(args: Array[String]) = {
    val n = args.length
    val m = n - 1
    var i = 0
    
    while(i < n) {
      val arg = args(i)
      if(arg startsWith "-") {
        if(i == m) {
          options += arg -> "true"
        } else {
          val value = args(i+1)
          if(value startsWith "-") {
            options += arg -> "true"
          } else {
            i += 1
            options += arg -> value
          }
        }
      } else { error("Argument error!") }
      i += 1
    }
  }
}
