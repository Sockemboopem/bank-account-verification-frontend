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

package bankaccountverification.connector

import bankaccountverification.AppConfig
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsError, JsSuccess, Json}
import uk.gov.hmrc.http._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

@Singleton
class BankAccountReputationConnector @Inject() (httpClient: HttpClient, appConfig: AppConfig) {

  private val bankAccountReputationConfig = appConfig.bankAccountReputationConfig

  def validateBankDetails(
    bankDetailsModel: BarsValidationRequest
  )(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Try[BarsValidationResponse]] = {
    import BarsValidationResponse._
    import HttpReads.Implicits.readRaw

    httpClient
      .POST[BarsValidationRequest, HttpResponse](
        url = bankAccountReputationConfig.validateBankDetailsUrl,
        body = bankDetailsModel
      )
      .map {
        case httpResponse if httpResponse.status == 200 =>
          Json.fromJson[BarsValidationResponse](httpResponse.json) match {
            case JsSuccess(result, _) =>
              Success(result)
            case JsError(errors) =>
              Failure(new HttpException("Could not parse Json response from BARs", httpResponse.status))
          }
        case httpResponse => Failure(new HttpException(httpResponse.body, httpResponse.status))
      }
      .recoverWith {
        case t: Throwable => Future.successful(Failure(t))
      }
  }
}