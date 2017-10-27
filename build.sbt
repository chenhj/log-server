import com.typesafe.sbt.packager.MappingsHelper.contentOf

name := "log-server"

version := "0.1"

scalaVersion := "2.12.3"

//gen package
enablePlugins(JavaServerAppPackaging)


lazy val versions= new {
    val finatra="2.13.0"
}


mappings in Universal <++= sourceDirectory map (src => contentOf(src / "main" / "resources").map(m=>(m._1,"conf/"+m._2) ))

bashScriptExtraDefines += """addJava "-Dconfig.file=${app_home}/../conf/application.conf""""

 libraryDependencies ++= Seq(
  "com.twitter" %% "finatra-http" % versions.finatra,
  "com.twitter" %% "finatra-httpclient" % versions.finatra,
  "ch.qos.logback" % "logback-classic" % "1.1.9",
  "com.typesafe" % "config" % "1.3.2",
   "io.getquill" % "quill-finagle-mysql_2.12" % "2.0.0",
   "com.twitter" % "finagle-redis_2.12" % "7.1.0",
  "org.scalatest" % "scalatest_2.12" % "3.2.0-SNAP9" % "test",
  "org.scalaz" % "scalaz-core_2.12" % "7.2.16"


)