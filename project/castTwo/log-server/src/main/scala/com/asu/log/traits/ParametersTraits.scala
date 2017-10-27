package com.asu.log.traits

import com.asu.log.exceptions.BadRequestException
import com.twitter.finagle.http.Request

import scala.util.{Failure, Success}

/**
  * Created by hjun  chenhj on 2017/10/26.
  */
sealed trait ParametersTraits {
    def tryGetParameter(request: Request,paramKey:String)=request.getParam(paramKey) match {
      case x:String =>Success(x)
      case _=>Failure(BadRequestException("Parameter '" ++ paramKey ++ " ' is requestd!"))
    }
}

trait LogTrait extends ParametersTraits{
    def getBusyType(request:Request)=tryGetParameter(request,"busyType")
 }


trait TokenTrait extends ParametersTraits{

}
