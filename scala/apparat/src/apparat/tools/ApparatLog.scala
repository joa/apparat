package apparat.tools

object ApparatLog {
  private val DEBUG = true
  
  private def write(value: String) = println(value)
  def close() = {}
  
  def apply(message: String) = info(message)
  def help(message: String) = write("[?] Help:\n" + message)
  def info(message: String) = write("[i] " + message)
  def succ(message: String) = write("[+] " + message)
  def warn(message: String) = write("[!] " + message)
  def err(message: String): Unit = write("[-] " + message)
  def err(throwable: Throwable): Unit = {
    err(throwable.getMessage)
    if(DEBUG) throwable.printStackTrace 
  }
}

private class ApparatLog {}
