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

	  in.async(_.copyDataTo(file1)).
	      flatMap(_ => file1.async(_.copyDataTo(file2))).
	      flatMap(_ => file1.async(_.copyDataTo(file3)))
	}
	
}