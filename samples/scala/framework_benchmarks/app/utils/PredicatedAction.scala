package utils

import play.api.mvc._
import scala.concurrent.Future

/**
 * A predicated action is one where a condition must be satisfied in order to proceed with the request. If the
 * condition is not satisfied then a supplied status result is yielded.
 */
class PredicatedActionBuilder {
  def apply[A](p: => Boolean, failed: => Future[SimpleResult])(action: Action[A]): Action[A] = new Action[A] {

    def apply(request: Request[A]): Future[SimpleResult] = {
      if (p) action(request) else failed
    }

    lazy val parser = action.parser
  }
}

object PredicatedAction extends PredicatedActionBuilder
