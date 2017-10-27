package com.asu.log

import com.asu.log.controllers.Api.{LogApi, UserApi}
import com.asu.log.filter.AuthFilter
import com.asu.log.modules.QuillContextModule
import com.google.inject.Module
import com.twitter.finatra.http.HttpServer
import com.twitter.finatra.http.routing.HttpRouter

/**
  * Created by hjun  chenhj on 2017/10/26.
  */
object App extends HttpServer{

  override protected def modules: Seq[Module] = Seq(QuillContextModule)

  override protected def defaultHttpServerName: String = "log-server"

  override protected def defaultFinatraHttpPort: String = ":8008"

  override protected def configureHttp(router: HttpRouter): Unit = {
      router.filter[AuthFilter]
          .add[UserApi]
          .add[LogApi]
  }
}
