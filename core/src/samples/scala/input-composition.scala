/**
 * These examples show various strategies for composing files together
 */
object InputComposition {

  /**
   * Zip the characters of two files together and find all that don't match
   */
  def zipAndCompare {
    import scalax.io.Resource
    
    val googleCom = Resource.fromURL("http://google.com").chars
    val googleCH = Resource.fromURL("http://google.ch").chars
    
    googleCom.zip(googleCH).filter{case (com,ch) => com != ch}
  }
  
  /**
   * Compare two inputs by comparing the first byte of each 100 byte block  
   */
  def blockCompare {
    import scalax.io.Resource

    val googleCom = Resource.fromURL("http://google.com").bytes
    val googleCH = Resource.fromURL("http://google.ch").bytes
    // NOTE:  maps, zips, sliding, collect, etc... are lazy until the data is accessed
    // so the following line does not access any data until forall (the following line)
    val blocks = googleCom.sliding(100,100).zip(googleCH.sliding(100,100))
    
    blocks.forall{case (com,ch) => com.head == ch.head}
  }
 
  /**
   * Deserialize two streams and perform a calculation based on the deserialized objects.
   * <p>
   * This example demonstrates how to transformation pipelines can be constructed lazily and
   * the resulting transformed streams can then be processed partially or fully depending on the
   * requirements.
   * </p><p>
   * The exact transformation example is to:
   * <ul>
   * <li>find a block of 100 bytes that starts with 1,2,3</li>
   * <li>transform each block to a "Holder" object which has a value associated with it based on the bytes in the 100 byte block</li>
   * <li>combine 2 streams one after the other</li>
   * <li>process the streams until the streams end or the cumulative value of the "Holder" objects > some value</li>
   * </ul>
   */
  def partialComposition {
    import scalax.io.{Resource, LongTraversable, End, Continue}
    val googleCom = Resource.fromURL("http://google.com")
    val googleCH = Resource.fromURL("http://google.ch")
    
    /* Obviously Holder is just silly in this example, but the idea is that you can lazy
     * construct a transformation pipeline that is not processed until the last step. This includes
     * deserialization of a input stream  
     */
    case class Holder(value:Int) {
      def this(serializedData:Traversable[Byte]) = this(serializedData.map(_.toInt).sum)
    }

    def process(seq:LongTraversable[Byte]) = seq.sliding(100).filter(_ startsWith Seq[Byte](1,2,3)).map(new Holder(_))
    val combined = process(googleCH.bytes) ++ process(googleCom.bytes)

    // Up to this point there has been no data access.  The fold will trigger the actual data access
    // Only as much of the streams as required will be read and the streams will be closed on terminiation
    val result = combined.limitFold(0){ (total,next) =>  
      if(total+next.value > 1000000) End(total)
      else Continue(total+next.value)
    }
  }
}