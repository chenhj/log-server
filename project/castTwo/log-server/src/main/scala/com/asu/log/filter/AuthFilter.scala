package com.asu.log.filter

import com.asu.log.API
import com.asu.log.helpers.RedisClientUtil
import com.asu.log.models.User
import com.google.inject.Inject
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.finatra.http.response.ResponseBuilder
import com.twitter.inject.Logging
import com.twitter.util.{Await, Future}

/**
  * Created by hjun  chenhj on 2017/10/26.
  */
class AuthFilter @Inject()(responseBuilder: ResponseBuilder) extends SimpleFilter[Request,Response] with Logging{

  def checkToken(request: Request):(Boolean,String)={
    val token=request.headerMap.get("token").getOrElse("not Found token!")
     logger.debug("token:"+token+",param:"+request.params.mkString("="))

    if(!API.getAuth) return (true,"pass")//是否启用授权验证
    if(!request.uri.contains("/auth/")) return (true,"pass")//请求过没有授权路径都通过

   val user=RedisClientUtil.read[User](API.getUserTokenKey(token))

   val result= Await.result[Option[User]](user)
    result match {
      case Some(u) =>(true,u.id)
      case None =>(false,"not auth")
    }

  }

  override def apply(request: Request, service: Service[Request, Response]):Future[Response] = {
    checkToken(request) match {
      case (false, mes) => responseBuilder.unauthorized(Some(Map("flag" -> "0", "code" -> "2", "message" -> Some(mes), "data" -> Some("")))).toFuture
      case (true, mes)  => service(request)
    }
  }
}
