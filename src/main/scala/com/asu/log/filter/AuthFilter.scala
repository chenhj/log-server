package com.asu.log.filter

import com.asu.log.API
import com.asu.log.helpers.RedisClientUtil
import com.asu.log.models.User
import com.google.inject.Inject
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.finatra.http.response.ResponseBuilder
import com.twitter.inject.Logging
import com.twitter.util.{Future, Return}

/**
  * Created by hjun  chenhj on 2017/10/26.
  */
class AuthFilter @Inject()(responseBuilder: ResponseBuilder) extends SimpleFilter[Request,Response] with Logging{

  def checkToken(request: Request):Future[(Boolean,String)]={
    val token=request.headerMap.get("token").getOrElse("")
     logger.debug("token:"+token+",param:"+request.params.mkString("="))

    if(!API.getAuth) return Future.value((true,"pass"))//是否启用授权验证
    if(!request.uri.contains("/auth/")) return Future.value((true,"pass"))//url中不包含"/auth/",直接通过

    if(token == "")return Future.value((false,"not found token"))

     RedisClientUtil.read[User](API.getUserTokenKey(token)) transform{
       case Return(Some(u)) =>Future.value((true,u.id))
       case Return(None) =>Future.value((false,"not auth"))
     }
    /**
      *
      此方式 ,存在并发请求阻塞情况
      val result= Await.result[Option[User]](user)
      result match {
        case Some(u) =>(true,u.id)
        case None =>(false,"not auth")
      }
      **/

  }

  override def apply(request: Request, service: Service[Request, Response]):Future[Response] = {
    checkToken(request).transform {
      case Return((false, mes)) => responseBuilder.unauthorized(Some(Map("flag" -> "0", "code" -> "2", "message" -> Some(mes), "data" -> Some("")))).toFuture
      case Return((true, mes))  => service(request)
    }
  }
}
