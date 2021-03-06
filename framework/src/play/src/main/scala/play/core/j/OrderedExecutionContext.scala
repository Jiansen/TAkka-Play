package play.core.j

import takka.actor.{ TypedActor, ActorSystem, Props }
import play.api.Logger
import play.core.Invoker
import play.mvc.Http
import scala.concurrent.ExecutionContext

/**
 * Executes work in a fixed-sized pool of actors. If an Http.Context is associated
 * with the current thread then that id will be used to dispatch work to the same
 * actor every time, resulting in ordered execution of work for that context.
 *
 * The ExecutionContext preserves the execution behaviour of F.Promise from Play 2.1.
 */
class OrderedExecutionContext(actorSystem: ActorSystem, size: Int) extends ExecutionContext {
  private val actors = Array.fill(size)(actorSystem.actorOf(Props[Runnable, OrderedExecutionContext.RunActor]))

  def execute(runnable: Runnable) = {
    val httpContext = Http.Context.current.get()
    val id: Long = if (httpContext == null) 0L else httpContext.id()
    val actor = actors((id % size).toInt)
    actor ! runnable
  }

  def reportFailure(t: Throwable) = Logger.error("Failure in OrderedExecutionContext", t)
}

object OrderedExecutionContext {
  /**
   * Used by the OrderedExecutionContext to run work in an actor.
   */
  class RunActor extends TypedActor[Runnable] {
    def typedReceive = {
      case runnable: Runnable => runnable.run()
    }
  }
}