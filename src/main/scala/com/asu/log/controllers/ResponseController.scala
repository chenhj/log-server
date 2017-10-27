package com.asu.log.controllers

import com.twitter.finatra.http.Controller
import com.twitter.inject.Logging
import com.twitter.util.{Future, Return, Throw}


/**
  * Created by hjun  chenhj on 2017/10/26.
  */
abstract class ResponseController extends Controller with Logging{

  case class FutureResponse(flag: String, message: String, code: String, data: Object) extends Serializable


  def success[A, Object](msessage: String, code: String, future: Future[Option[A]]): Future[Option[FutureResponse]] =
    future.transform {
      case Return(Some(value)) => Future.value[Option[FutureResponse]](Some(FutureResponse("1", message = msessage, code = code, data = Some(value))))
      case Return(value)       => Future.value[Option[FutureResponse]](Some(FutureResponse("1", message = msessage, code, value)))
      case Return(None)        => Future.value[Option[FutureResponse]](Some(FutureResponse("1", message = msessage, code, "")))
      case Throw(origThrowable) =>
        Future.value[Option[FutureResponse]](Some(FutureResponse("0", message = msessage, code, "")))
    }

  def success[A, Object](future: Future[Option[A]]): Future[Option[FutureResponse]] = success[A, Object]("操作成功", "", future)

  def successList[A, Object](msessage: String, code: String, future: Future[Seq[A]]): Future[Option[FutureResponse]] =
    future.transform {
      //      case Return(Seq(value :: Nil)) => Future.value[Option[FutureResponse]](Some(FutureResponse("1", message = msessage, code = code, data = Some(value))))
      case Return(Nil)   => Future.value[Option[FutureResponse]](Some(FutureResponse("1", message = msessage, code, "")))
      case Return(value) => Future.value[Option[FutureResponse]](Some(FutureResponse("1", message = msessage, code, value)))
      case Throw(origThrowable) =>
        //  log.warn("Failed future " + origThrowable + " converted into " + throwable)
        //Future.exception(throwable)
        Future.value[Option[FutureResponse]](Some(FutureResponse("0", message = msessage, code, "")))
    }

  def fail[A, Object](msessage: String, code: String, future: Future[Option[A]]): Future[Option[FutureResponse]] =
    future.transform {
      case Return(Some(value)) => Future.value[Option[FutureResponse]](Some(FutureResponse("0", message = msessage, code = code, data = None)))
      case Return(None)        => Future.value[Option[FutureResponse]](Some(FutureResponse("0", message = msessage, code, "")))
      case Throw(origThrowable) =>
        Future.value[Option[FutureResponse]](Some(FutureResponse("0", message = msessage, code, "")))
    }



  //  def checkRequestType(request:RequestType)(callback:Request=>Future[ResponseBuilder])=
//    request.headerMap.get("Accept").getOrElse("").split(",").map(_.trim) match {
//      case array if array.contains("*/*") || array.contains("application/json")=>callback(request)
//      case _=>throw new UnsupportedOperationException
//    }

}
