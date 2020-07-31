/*
 * Copyright 2020 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.bankaccountverificationfrontend.controllers

import java.util.UUID

import com.codahale.metrics.SharedMetricRegistries
import org.scalatest.OptionValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Configuration, Environment}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.bankaccountverificationfrontend.config.AppConfig
import uk.gov.hmrc.bankaccountverificationfrontend.store.MongoSessionRepo
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents

import scala.concurrent.duration._
import scala.util.Success

class ApiControllerSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with OptionValues {
  implicit private val timeout: FiniteDuration = 1 second

  private val env           = Environment.simple()
  private val configuration = Configuration.load(env)

  private val serviceConfig = new ServicesConfig(configuration)
  private val appConfig     = new AppConfig(configuration, serviceConfig)

  override lazy val app = {
    SharedMetricRegistries.clear()
    fakeApplication()
  }

  private lazy val sessionStore = app.injector.instanceOf[MongoSessionRepo]

  private val controller =
    new ApiController(appConfig, stubMessagesControllerComponents(), sessionStore)

  "GET /start" should {
    "return 200" in {
      val fakeRequest = FakeRequest("GET", "/api/init").withMethod("POST")
      val result      = controller.init().apply(fakeRequest)
      status(result) shouldBe Status.OK
      val journeyIdMaybe = contentAsString(result)
      journeyIdMaybe                                should not be ""
      BSONObjectID.parse(journeyIdMaybe).toOption shouldBe defined
    }
  }
}
