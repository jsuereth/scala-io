/**
 * Examples of Asynchronous IO.
 */
object AsyncIO {
	def asyncCopy {
	  import scalax.io.{Resource, Input}
	  
	  val in:Input = Resource.fromURL("http://www.google.com")
	  val future = in.async {resource =>
	    resource.copyDataTo(Resource.fromFile("/tmp/x"))
	  }
	  
	  // Execute a silly little println when done reading data
	  future.onComplete(f => println("done copying"))
	}
	def asyncWrite {
	  import scalax.io.{Resource, Output}
	  val out:Output = Resource.fromFile("/tmp/file")
	  val future = out.async{ resource =>
	    resource.write("Writing something silly")
	  }

	  // essentially block for writing.  Kind of defeats the purpose of async, but sometimes you need to wait for an operation to finish.
	  future.get
	}

	def combiningFutures {
	  import scalax.io.{Resource, Output, Input}
	  val in = Resource.fromURL("http://www.google.com")
	  val file1 = Resource.fromFile("/tmp/file1")
	  val file2 = Resource.fromFile("/tmp/file2")
	  val file3 = Resource.fromFile("/tmp/file3")

	  // this example copies the url to file1 then from file1 to file2 and file3.  
	  // when composedFuture1 is complete all the copies are done
	  val composedFuture1 = in.async(_.copyDataTo(file1)).
	      flatMap(_ => file1.async(_.copyDataTo(file2))).
	      flatMap(_ => file1.async(_.copyDataTo(file3)))
	      
	  composedFuture1 onComplete (_ => println("finished copying"))
	  
	  // this example does the same thing except the future will contain the 3
	  // files that contain the copies (file1,file2,file3).   
	  val composedFuture2 = for{
	    file1 <- in.async{in => in.copyDataTo(file1); file1}
	    file2 <- file1.async{f1 => f1.copyDataTo(file2); file2}
	    file3 <- file1.async{f1 => f1.copyDataTo(file3); file3}
	  } yield (file1,file2,file3)

	  
	  
	}
	
}