package scalax.io

/**
 * A future modelled after the Akka future.
 */
trait Future[+T] {
	def get:T
	def onComplete(func: (Future[T]) => Unit): Future[T]
	def value: Option[Either[Throwable, T]]
	def isCompleted: Boolean
	def map [A] (f: (T) ⇒ A): Future[A]
	def flatMap [A] (f: (T) ⇒ Future[A]): Future[A]
	def foreach [U] (f: (T) ⇒ U): Unit
}