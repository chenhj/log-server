package com.asu

import org.joda.time.DateTime
import org.scalatest.FunSuite

/**
  * Created by hjun  chenhj on 2017/10/27.
  */
class test extends FunSuite{

  test(" test data "){

      val d=DateTime.now()
    println("centuryOfEra:"+d.centuryOfEra().get())
    println("dayOfMonth:"+d.dayOfMonth().get())
    println("dayOfWeek:"+d.dayOfWeek().get())
    println("dayOfYear:"+d.dayOfYear().get())
    println("getYear:"+d.getYear)
    println("millisOfDay:"+d.millisOfDay().get())
    println("monthOfYear:"+d.monthOfYear().get())
    println("dayOfMonth:"+d.dayOfMonth().get())

      print("test data")
  }

}
