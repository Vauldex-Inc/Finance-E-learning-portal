package tech.vauldex
package controllers

import java.util.UUID
import javax.inject.{ Inject, Singleton }
import scala.concurrent.{ ExecutionContext, Future }
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.libs.json.Json
import tech.vauldex._
import models.domain._
import models.service.SectionService
import security._
import errors.NDErrorHandler

@Singleton
class SectionController @Inject() (
    secureAction: SecureAction,
    sectionService: SectionService,
    val controllerComponents: ControllerComponents
)(implicit protected val ec: ExecutionContext)
    extends BaseController
    with play.api.i18n.I18nSupport {

  private val sectionForm = Form(
    tuple(
      "id" -> optional(uuid),
      "name" -> text.verifying(nonEmpty),
      "courseId" -> optional(uuid),
      "order" -> optional(number)
    )
  )

  private val editSectionForm = Form(
    tuple(
      "id" -> uuid,
      "name" -> optional(text),
      "courseId" -> optional(uuid),
      "order" -> optional(number)
    )
  )

  def create = secureAction.async { implicit request =>
    sectionForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(formWithErrors.errorsAsJson))
      },
      sections => {
        sectionService.create(sections._1, sections._2, sections._3, sections._4 ).fold(NDErrorHandler(_), _ => Created)
      }
    )
  }

  def update = secureAction.async { implicit request =>
    editSectionForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(formWithErrors.errorsAsJson))
      },
      sections => {
        sectionService.update(
          sections._1,
          sections._2,
          sections._3,
          sections._4)
        .map(_ => Ok)
      }
    )
  }

  def get = secureAction.async { implicit request =>
    sectionService.getAll().map(sections => Ok(Json.toJson(sections)))
  }

  def delete (id: UUID) = secureAction.async { implicit request =>
    sectionService.delete(id).map(_ => Ok)
  }

}
