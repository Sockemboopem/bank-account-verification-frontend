/*
 * Copyright 2021 HM Revenue & Customs
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

package bankaccountverification

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

import bankaccountverification.api._
import bankaccountverification.connector.ReputationResponseEnum.{Error, Indeterminate}
import bankaccountverification.connector._
import bankaccountverification.web.AccountTypeRequestEnum
import bankaccountverification.web.AccountTypeRequestEnum.{Business, Personal}
import bankaccountverification.web.business.BusinessVerificationRequest
import bankaccountverification.web.personal.PersonalVerificationRequest
import play.api.libs.json.{JsValue, Json}

case class TimeoutConfig(timeoutUrl: String, timeoutAmount: Int, timeoutKeepAliveUrl: Option[String])

case class PrepopulatedData(accountType: AccountTypeRequestEnum, name: Option[String],
                            sortCode: Option[String], accountNumber: Option[String], rollNumber: Option[String])

case class Address(lines: List[String], town: Option[String], postcode: Option[String])

case class Session(accountType: Option[AccountTypeRequestEnum] = None, address: Option[Address] = None,
                   personal: Option[PersonalAccountDetails] = None, business: Option[BusinessAccountDetails] = None)

object Session {
  def toCompleteResponseJson(session: Session): Option[JsValue] =
    session.accountType match {
      case Some(AccountTypeRequestEnum.Personal) => PersonalAccountDetails.toCompleteResponse(session).map(Json.toJson(_))
      case Some(AccountTypeRequestEnum.Business) => BusinessAccountDetails.toCompleteResponse(session).map(Json.toJson(_))
    }

  def toCompleteV2ResponseJson(session: Session): Option[JsValue] =
    session.accountType match {
      case Some(AccountTypeRequestEnum.Personal) => PersonalAccountDetails.toCompleteV2Response(session).map(Json.toJson(_))
      case Some(AccountTypeRequestEnum.Business) => BusinessAccountDetails.toCompleteV2Response(session).map(Json.toJson(_))
    }
}

case class PersonalAccountDetails(accountName: Option[String],
                                  sortCode: Option[String],
                                  accountNumber: Option[String],
                                  rollNumber: Option[String] = None,
                                  accountNumberWithSortCodeIsValid: Option[ReputationResponseEnum] = None, // This property to be removed once all BARS sessions are in the new format (TAV-458)
                                  accountNumberIsWellFormatted: Option[ReputationResponseEnum] = None,
                                  accountExists: Option[ReputationResponseEnum] = None,
                                  nameMatches: Option[ReputationResponseEnum] = None,
                                  nonStandardAccountDetailsRequiredForBacs: Option[ReputationResponseEnum] = None,
                                  sortCodeBankName: Option[String] = None,
                                  sortCodeSupportsDirectDebit: Option[ReputationResponseEnum] = None,
                                  sortCodeSupportsDirectCredit: Option[ReputationResponseEnum] = None)

object PersonalAccountDetails {
  def apply(request: PersonalVerificationRequest, response: BarsPersonalAssessResponse): PersonalAccountDetails =
    response match {
      case success: BarsPersonalAssessSuccessResponse =>
        PersonalAccountDetails(
          Some(request.accountName),
          Some(request.sortCode),
          Some(request.accountNumber),
          request.rollNumber,
          None, // This property to be removed once all BARS sessions are in the new format (TAV-458)
          Some(success.accountNumberIsWellFormatted),
          Some(success.accountExists),
          Some(success.nameMatches),
          success.nonStandardAccountDetailsRequiredForBacs,
          success.sortCodeBankName,
          Some(success.sortCodeSupportsDirectDebit),
          Some(success.sortCodeSupportsDirectCredit))
      case _ =>
        PersonalAccountDetails(
          Some(request.accountName), Some(request.sortCode), Some(request.accountNumber), request.rollNumber,
          None, // This property to be removed once all BARS sessions are in the new format (TAV-458)
          Some(Error), Some(Error), Some(Error), Some(Error), None, Some(Error), Some(Error))
    }

  def toCompleteResponse(session: Session): Option[CompleteResponse] =
    session match {
      case Session(
      _,
      address,
      Some(PersonalAccountDetails(
      Some(accountName),
      Some(sortCode),
      Some(accountNumber),
      rollNumber,
      maybeAccountNumberWithSortCodeIsValid,
      maybeAccountNumberIsWellFormatted,
      accountExists,
      nameMatches,
      nonStandardAccountDetailsRequiredForBacs,
      sortCodeBankName,
      sortCodeSupportsDirectDebit,
      sortCodeSupportsDirectCredit)),
      _
      ) if maybeAccountNumberWithSortCodeIsValid.isDefined | maybeAccountNumberIsWellFormatted.isDefined =>
        Some(
          CompleteResponse(
            Personal,
            Some(
              api.PersonalCompleteResponse(
                address.map(a => CompleteResponseAddress(a.lines, a.town, a.postcode)),
                accountName, sortCode, accountNumber,
                maybeAccountNumberIsWellFormatted.orElse(maybeAccountNumberWithSortCodeIsValid).get,
                rollNumber, accountExists, nameMatches,
                Some(Indeterminate), Some(Indeterminate), Some(Indeterminate), // Hardcode these to be indeterminate (TAV-458)
                nonStandardAccountDetailsRequiredForBacs, sortCodeBankName, sortCodeSupportsDirectDebit,
                sortCodeSupportsDirectCredit)),
            None))
      case _ => None
    }

  def toCompleteV2Response(session: Session): Option[CompleteV2Response] =
    session match {
      case Session(
      _,
      address,
      Some(PersonalAccountDetails(
      Some(accountName),
      Some(sortCode),
      Some(accountNumber),
      rollNumber,
      maybeAccountNumberWithSortCodeIsValid,
      maybeAccountNumberIsWellFormatted,
      accountExists,
      nameMatches,
      nonStandardAccountDetailsRequiredForBacs,
      sortCodeBankName,
      sortCodeSupportsDirectDebit,
      sortCodeSupportsDirectCredit)),
      _
      ) if maybeAccountNumberWithSortCodeIsValid.isDefined | maybeAccountNumberIsWellFormatted.isDefined =>
        Some(
          CompleteV2Response(
            Personal,
            Some(
              api.PersonalCompleteV2Response(
                accountName, sortCode, accountNumber,
                maybeAccountNumberIsWellFormatted.orElse(maybeAccountNumberWithSortCodeIsValid).get,
                rollNumber, accountExists, nameMatches, nonStandardAccountDetailsRequiredForBacs, sortCodeBankName,
                sortCodeSupportsDirectDebit, sortCodeSupportsDirectCredit)),
            None))
      case _ => None
    }
}

case class BusinessAccountDetails(companyName: Option[String],
                                  sortCode: Option[String],
                                  accountNumber: Option[String],
                                  rollNumber: Option[String] = None,
                                  accountNumberWithSortCodeIsValid: Option[ReputationResponseEnum] = None, // This property to be removed once all BARS sessions are in the new format (TAV-458)
                                  accountNumberIsWellFormatted: Option[ReputationResponseEnum] = None,
                                  accountExists: Option[ReputationResponseEnum] = None,
                                  companyNameMatches: Option[ReputationResponseEnum] = None, // This property to be removed once all BARS sessions are in the new format (TAV-458)
                                  nameMatches: Option[ReputationResponseEnum] = None,
                                  nonStandardAccountDetailsRequiredForBacs: Option[ReputationResponseEnum] = None,
                                  sortCodeBankName: Option[String] = None,
                                  sortCodeSupportsDirectDebit: Option[ReputationResponseEnum] = None,
                                  sortCodeSupportsDirectCredit: Option[ReputationResponseEnum] = None)

object BusinessAccountDetails {
  def apply(request: BusinessVerificationRequest, response: BarsBusinessAssessResponse): BusinessAccountDetails =
    response match {
      case success: BarsBusinessAssessSuccessResponse =>
        BusinessAccountDetails(
          Some(request.companyName),
          Some(request.sortCode),
          Some(request.accountNumber),
          request.rollNumber,
          None, // This property to be removed once all BARS sessions are in the new format (TAV-458)
          Some(success.accountNumberIsWellFormatted),
          success.nonStandardAccountDetailsRequiredForBacs,
          Some(success.accountExists),
          None,
          Some(success.nameMatches),
          success.sortCodeBankName,
          Some(success.sortCodeSupportsDirectDebit),
          Some(success.sortCodeSupportsDirectCredit))
      case _ =>
        BusinessAccountDetails(
          Some(request.companyName), Some(request.sortCode), Some(request.accountNumber), request.rollNumber,
          None, // This property to be removed once all BARS sessions are in the new format (TAV-458)
          Some(Error), None, Some(Error),
          None, // This property to be removed once all BARS sessions are in the new format (TAV-458)
          Some(Error), None, Some(Error), Some(Error))
    }

  def toCompleteResponse(session: Session): Option[CompleteResponse] =
    session match {
      case Session(
      _,
      address,
      _,
      Some(BusinessAccountDetails(
      Some(companyName),
      Some(sortCode),
      Some(accountNumber),
      rollNumber,
      maybeAccountNumberWithSortCodeIsValid,
      maybeAccountNumberIsWellFormatted,
      accountExists,
      companyNameMatches,
      nameMatches,
      nonStandardAccountDetailsRequiredForBacs,
      sortCodeBankName,
      sortCodeSupportsDirectDebit,
      sortCodeSupportsDirectCredit))
      ) if maybeAccountNumberWithSortCodeIsValid.isDefined | maybeAccountNumberIsWellFormatted.isDefined =>
        Some(
          CompleteResponse(
            Business,
            None,
            Some(
              BusinessCompleteResponse(
                address.map(a => CompleteResponseAddress(a.lines, a.town, a.postcode)),
                companyName, sortCode, accountNumber, rollNumber,
                maybeAccountNumberIsWellFormatted.orElse(maybeAccountNumberWithSortCodeIsValid).get,
                accountExists,
                nameMatches.orElse(companyNameMatches),
                Some(Indeterminate), Some(Indeterminate), // Hardcode these to be indeterminate (TAV-458)
                nonStandardAccountDetailsRequiredForBacs, sortCodeBankName, sortCodeSupportsDirectDebit,
                sortCodeSupportsDirectCredit))))
      case _ =>
        None
    }

  def toCompleteV2Response(session: Session): Option[CompleteV2Response] =
    session match {
      case Session(
      _,
      address,
      _,
      Some(BusinessAccountDetails(
      Some(companyName),
      Some(sortCode),
      Some(accountNumber),
      rollNumber,
      maybeAccountNumberWithSortCodeIsValid,
      maybeAccountNumberIsWellFormatted,
      accountExists,
      companyNameMatches,
      nameMatches,
      nonStandardAccountDetailsRequiredForBacs,
      sortCodeBankName,
      sortCodeSupportsDirectDebit,
      sortCodeSupportsDirectCredit))
      ) if maybeAccountNumberWithSortCodeIsValid.isDefined | maybeAccountNumberIsWellFormatted.isDefined =>
        Some(
          CompleteV2Response(
            Business,
            None,
            Some(
              BusinessCompleteV2Response(
                companyName, sortCode, accountNumber, rollNumber,
                maybeAccountNumberIsWellFormatted.orElse(maybeAccountNumberWithSortCodeIsValid).get,
                accountExists,
                nameMatches.orElse(companyNameMatches),
                nonStandardAccountDetailsRequiredForBacs, sortCodeBankName,
                sortCodeSupportsDirectDebit, sortCodeSupportsDirectCredit))))
      case _ =>
        None
    }
}