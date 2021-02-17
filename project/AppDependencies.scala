import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc" %% "simple-reactivemongo" % "7.30.0-play-27",
    "uk.gov.hmrc" %% "bootstrap-frontend-play-27" % "3.0.0",
    "uk.gov.hmrc" %% "play-frontend-hmrc" % "0.43.0-play-27",
    "uk.gov.hmrc" %% "play-partials" % "6.11.0-play-27",
    "uk.gov.hmrc" %% "play-language" % "4.4.0-play-27"
  )

  val test = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-27" % "3.0.0" % Test,
    "org.scalatest" %% "scalatest" % "3.1.2" % Test,
    "org.jsoup" % "jsoup" % "1.10.2" % Test,
    "com.typesafe.play" %% "play-test" % current % Test,
    "org.scalatestplus" %% "mockito-3-2" % "3.1.2.0" % "test, it",
    "com.vladsch.flexmark" % "flexmark-all" % "0.35.10" % "test, it",
    "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.3" % "test, it",
    "uk.gov.hmrc" %% "reactivemongo-test" % "4.21.0-play-27" % "test, it"
  )
}
