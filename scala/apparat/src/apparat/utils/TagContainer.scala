package apparat.utils

import apparat.swc._
import apparat.swf._
import apparat.utils.IO._
import java.io._

object TagContainer {
  def fromFile(pathname: String): TagContainer = fromFile(new File(pathname))
  def fromFile(file: File): TagContainer = {
    val tc = new TagContainer
    tc read file
    tc
  }
}

class TagContainer {
  var strategy: Option[TagContainerStrategy] = None
  
  def tags: List[SwfTag] = strategy match {
    case Some(x) => x.tags
    case None => List()
  }
  
  private def strategyFor(file: File) = file.getName() match {
    case x if x endsWith ".swf" => Some(new SwfStrategy)
    case x if x endsWith ".swc" => Some(new SwcStrategy)
    case _ => None
  }
  
  
  def read(pathname: String): Unit = read(new File(pathname))
  def read(file: File): Unit = {
    strategy = strategyFor(file)
    strategy match {
      case Some(x) => {
        using(new FileInputStream(file))(input => {
          x read (input, file length)
        })
      }
      case None => {}
    }
  }
  
  
  def write(pathname: String): Unit = write(new File(pathname)) 
  def write(file: File): Unit = {
    strategy match {
      case Some(x) => {
        using(new FileOutputStream(file)) (output => {
          x write output
        })       
      }
      case None	 => {}
    }
  }
}

trait TagContainerStrategy {
  def read(input: InputStream, length: Long)
  def write(output: OutputStream)
  def tags: List[SwfTag]
}

private class SwfStrategy extends TagContainerStrategy {
  var swf: Option[Swf] = None
    
  override def tags: List[SwfTag] = swf match {
    case Some(x) => x.tags
    case None => Nil
  }
  
  override def read(input: InputStream, length: Long) = {
    swf = Some(Swf fromInputStream (input, length)) 
  }
  
  override def write(output: OutputStream) = {
    swf match {
      case Some(x) => x write output
      case None => {}
    }
  }
}

private class SwcStrategy extends TagContainerStrategy {
  var swc: Option[Swc] = None
  var swf: Option[Swf] = None
    
  override def tags: List[SwfTag] = swf match {
    case Some(x) => x.tags
    case None => Nil
  }
  
  override def read(input: InputStream, length: Long) = {
    swc = Some(Swc fromInputStream input)
    swf = Some(Swf fromSwc swc.getOrElse(error("Could not read SWC."))) 
  }
  
  override def write(output: OutputStream) = {
    swc match {
      case Some(x) => {
       swf match {
         case Some(y) => {
           y write x
         }
         case None => {}
       }
       x write output
      }
      case None => {}
    }
  }
}