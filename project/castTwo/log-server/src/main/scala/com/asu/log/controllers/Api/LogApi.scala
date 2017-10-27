package com.asu.log.controllers.Api

import com.asu.log.API
import com.asu.log.controllers.ResponseController
import com.asu.log.service.LogService
import com.asu.log.traits.LogTrait
import com.google.inject.Inject
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.request.RequestUtils
import com.twitter.util.Future


import scala.util.{Failure, Success, Try}


/**
  * Created by hjun  chenhj on 2017/10/26.
  */
class LogApi @Inject()(logService: LogService) extends ResponseController with LogTrait {

   post(API.getBaseUrl ++ "/cast/auth/upload"){ request:Request =>
    val mc=   RequestUtils.multiParams(request).get("logfile")
      (for{
         typ <-Try(getBusyType(request).get)
         result<-Try(logService.saveFile(mc,typ))
      } yield result ) match {
         case Failure(error) =>{
           logger.error("error:" + error.getMessage)
           fail("save file failure:" + error.getMessage,"",Future.None)
         }
         case Success(value)=>  success("ok","",value)
      }
   }


}
