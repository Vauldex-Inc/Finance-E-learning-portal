package tech.vauldex
package controllers

import java.time.LocalDateTime
import javax.inject.{ Inject, Named, Singleton }
import scala.concurrent.{ ExecutionContext, Future }
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.libs.json._
import tech.vauldex._
import models.domain._
import models.service._
import security._
import errors.NDErrorHandler

@Singleton
class MemberController @Inject() (
    secureAction: SecureAction,
    memberService: MemberService,
    val controllerComponents: ControllerComponents
)(implicit protected val ec: ExecutionContext)
    extends BaseController
    with play.api.i18n.I18nSupport {

  private val memberForm = Form(
    tuple(
      "firstName" -> optional(text),
      "lastName" -> optional(text),
      "furiganaFirst" -> optional(text),
      "furiganaLast" -> optional(text),
      "phoneNumber" -> optional(text(minLength = 10, maxLength = 11)),
      "residenceInfo" -> optional(text(3)),
      "birthDate" -> optional(localDate),
      "twitterId" -> optional(text(3)),
      "displayName" -> optional(text)
    ))

  private val tutorialForm = Form(
    single(
      "key" -> text
  ))

  private val idForm = Form(single("id" -> text.verifying(nonEmpty)))

  def update = secureAction.async { implicit request =>
    val id = request.userId
    memberForm
      .bindFromRequest()
      .fold(
        formWithErrors => {
          Future.successful(BadRequest(formWithErrors.errorsAsJson))
        },
        member => {
          memberService.update(
            request.userId,
            member._1,
            member._2,
            member._3,
            member._4,
            member._5,
            member._6,
            member._7,
            member._8,
            member._9)
          .map {
            _ => Ok
          }
        }
      )
  }

  def updateTutorial = secureAction.async { implicit request =>
    tutorialForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(formWithErrors.errorsAsJson))
      },
      tutorial => {
          memberService.updateTutorial(request.userId, tutorial)
          .fold(NDErrorHandler(_), _ => {
            Ok
          })
        }
    )
  }

  def find = secureAction.async { request =>
    memberService.find(request.userId).fold(NDErrorHandler(_), {
      case (user, member) => Ok(Json.obj("user" -> user, "member" -> member))
    })
  }

  def get = secureAction.async { implicit request =>
    memberService
      .get(request.userId)
      .fold(
        NDErrorHandler(_),
        {
          case (member, collectionSeq) => {
            val collectionJson = collectionSeq.map(collection => {
              Json.toJson(
                Json.obj(
                  "collectionItemName" -> collection._1,
                  "unlocked" -> collection._2
                )
              )
            })

            Ok(
              Json.obj(
                "member" -> Json.toJson(member),
                "collectionStatus" -> collectionJson
              )
            )
          }
        }
      )
  }

  def send = Action.async { implicit request =>
    idForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(formWithErrors.errorsAsJson))
      },
      id => {
        memberService.sendBadge(id).fold(NDErrorHandler(_), {
          _ => Ok
        })
      }
    )
  }
}
