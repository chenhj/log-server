import com.typesafe.sbt.packager.MappingsHelper.contentOf

name := "log-server"

version := "0.1"

scalaVersion := "2.12.3"

//gen package
enablePlugins(JavaServerAppPackaging)


lazy val versions= new {
    val finatra="2.13.0"
}
//
//mappings in Universal += {
//  // we are using the reference.conf as default application.conf
//  // the user can override settings here
//  val conf = (resourceDirectory in Compile).value / "reference.conf"
//  conf -> "conf/application.conf"
//}

mappings in Universal <++= sourceDirectory map (src => contentOf(src / "main" / "resources").map(m=>(m._1,"conf/"+m._2) ))

bashScriptExtraDefines += """addJava "-Dconfig.file=${app_home}/../conf/application.conf""""


//fork in run := true
//javaOptions ++= Seq(  "-Dlog.service.output=/dev/stderr",  "-Dlog.access.output=/dev/stderr")
libraryDependencies ++= Seq(
  "com.twitter" %% "finatra-http" % versions.finatra,
  "com.twitter" %% "finatra-httpclient" % versions.finatra,
  // By placing this in the classpath, spans report to localhost:9411
  // you can change via -Dzipkin.http.host=your_host:9411
 // "io.zipkin.finagle" %% "zipkin-finagle-http" % "1.0.0",
 // "io.zipkin.finagle" %% "zipkin-finagle" % "1.0.0",
 // "com.google.auto.service" % "auto-service" % "1.0-rc3",
 // "com.google.auto.value" % "auto-value" % "1.4.1" % "provided",
  "ch.qos.logback" % "logback-classic" % "1.1.9",
  "com.typesafe" % "config" % "1.3.2",

  //dbs
   "io.getquill" % "quill-finagle-mysql_2.12" % "2.0.0",
  //redis
   "com.twitter" % "finagle-redis_2.12" % "7.1.0",

  //test
  "org.scalatest" % "scalatest_2.12" % "3.2.0-SNAP9" % "test",
  //
  "org.scalaz" % "scalaz-core_2.12" % "7.2.16"


)