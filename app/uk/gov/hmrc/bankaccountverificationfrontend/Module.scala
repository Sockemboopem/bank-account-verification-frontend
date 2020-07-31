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

package uk.gov.hmrc.bankaccountverificationfrontend

import com.google.inject.{AbstractModule, Provides}
import javax.inject.Singleton
import play.api.{Configuration, Environment}
import play.api.libs.concurrent.AkkaGuiceSupport
import uk.gov.hmrc.bankaccountverificationfrontend.config.AppConfig
import uk.gov.hmrc.bankaccountverificationfrontend.store.MongoSessionRepo

class Module(environment: Environment, playConfig: Configuration) extends AbstractModule with AkkaGuiceSupport {
  override def configure(): Unit = {
    super.configure()
    bind(classOf[AppConfig])
    bind(classOf[MongoSessionRepo])

    @Provides
    @Singleton
    def provideLogger: SimpleLogger = new LoggerFacade(play.api.Logger.logger)

  }
}