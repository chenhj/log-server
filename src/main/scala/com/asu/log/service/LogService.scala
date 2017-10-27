package com.asu.log.service

import java.io.File
import java.util.Date

import com.asu.log.API
import com.google.inject.Inject
import com.twitter.finatra.http.fileupload.MultipartItem
import com.twitter.inject.Logging
import com.twitter.util.Future
import org.apache.commons.io.FileUtils
import org.apache.commons.lang.time.DateFormatUtils
import org.joda.time.DateTime

/**
  * Created by hjun  chenhj on 2017/10/26.
  */
class LogService  @Inject()() extends Logging{

  def getBuseType(typ:String):String=
    typ match {
      case "1"=>"syslog"
      case "2"=>"applog"
      case "3"=>"castlog"
      case _=>"otherlog"
    }

  private def getFilePath(typ:String,fname:String):String={
    //存放路径/业务类型/年月/日期/文件名称
    val d= DateTime.now()
    val datepath=d.getYear +""+ d.monthOfYear().get() +File.separator + DateFormatUtils.format(new Date(),"yyyyMMdd")
     API.getBasePath +File.separator + getBuseType(typ) +File.separator + datepath+File.separator+fname
  }


  def saveFile(mc: Option[MultipartItem],typ:String): Future[Option[String]]= {
    mc match {
      case Some(m) =>
        val path=getFilePath(typ,m.filename.getOrElse("tmep.log"))
        val f=new File(path)
        FileUtils.writeByteArrayToFile(f,m.data)
        logger.debug("save file:"+path)
        Future.value(Some("save ok"))
      case None =>Future.None
    }
  }




}
