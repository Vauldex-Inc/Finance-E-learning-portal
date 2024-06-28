package tech.vauldex
package controllers

import java.util.UUID
import javax.inject.{ Inject, Singleton }
import scala.concurrent.{ ExecutionContext, Future }
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.libs.json.{ Json, Writes }
import tech.vauldex._
import models.domain._
import models.service.CourseService
import security._
import errors.NDErrorHandler

@Singleton
class CourseController @Inject() (
    secureAction: SecureAction,
    courseService: CourseService,
    val controllerComponents: ControllerComponents
)(implicit protected val ec: ExecutionContext)
    extends BaseController
    with play.api.i18n.I18nSupport {

  private val courseForm = Form(single("name" -> text.verifying(nonEmpty)))
  private val editForm = Form(
    tuple(
      "id" -> uuid,
      "name" -> optional(text),
      "introduction" -> default(text, "")
    )
  )
  def create = secureAction.async { implicit request =>
    courseForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(formWithErrors.errorsAsJson))
      },
      name => {
        courseService.create(name).fold(NDErrorHandler(_), _ => Created)
      }
    )
  }

  def get = secureAction.async { implicit request =>
    val offset = request.getQueryString("offset").map(_.toInt).getOrElse(0)
    val limit = request.getQueryString("limit").map(_.toInt).getOrElse(10)

    courseService.getAll(offset, limit).map(courses => Ok(Json.toJson(courses)))
  }

  def getLength = secureAction.async { implicit request =>
    courseService.getLength.map(length => Ok(Json.obj("size" -> length)))
  }

  def getCourseNav = secureAction.async { implicit request =>
    courseService.getCourseNav.map{ courses => {
      val mapped = courses.map{ course => {
        Json.obj("id"->course._2,"name"->course._3,"firstLessonUrl"->course._4)
      } }
    
      Ok(Json.toJson(mapped))
    }}
  }

  def getById(id: UUID) = secureAction.async { implicit request =>
    courseService.getById(id).fold(NDErrorHandler(_),  course => Ok(Json.obj("data" -> course)))
  }

  def edit = secureAction.async { implicit request =>
    editForm.bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest(formWithErrors.errorsAsJson)),
      course => courseService.edit(course._1, course._2, course._3).map(_ => Ok)
    )
  }
  def delete(id: UUID) = secureAction.async { implicit request =>
    courseService.deleteById(id).map(_ => Ok)
  }
}
