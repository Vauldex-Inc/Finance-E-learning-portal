package tech.vauldex
package models.service

import java.util.UUID
import java.time.Instant
import javax.inject.{ Inject, Singleton }
import scala.concurrent.{ ExecutionContext, Future }
import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import play.api.http.Status._
import play.api.mvc._
import cats.implicits._
import cats.data.{ OptionT, EitherT }
import tech.vauldex._
import models.domain._
import models.repo._
import models.profile.PostgresProfile
import errors.NDError
import utils._

@Singleton
final class CourseService @Inject() (
    val courseRepo: CourseRepo,
    val sectionRepo: SectionRepo,
    val lessonRepo: LessonRepo,
    val userLessonHistoryRepo: UserLessonHistoryRepo,
    protected val dbConfigProvider: DatabaseConfigProvider
)(implicit protected val ec: ExecutionContext)
    extends HasDatabaseConfigProvider[PostgresProfile] {
  import profile.api._

  def create(name: String): NDAResult[Unit] = {
    for {
      _ <- ioActionResult(courseRepo.create(Course(UUID.randomUUID, name, None, UUID.randomUUID(), Instant.now, Instant.now)), NDError("error.bad_request", NOT_FOUND, Some("BAD_REQUEST")), ())
    } yield ()
  }

  def getAll(offset: Int, limit: Int): Future[Seq[Course]] = {
    for {
      courses <- courseRepo.all(offset, limit)
    } yield courses
  }

  def getLength = {
    for {
      length <- courseRepo.getLength
    } yield length
  }

  def getCourseNav: Future[Seq[(Instant, UUID, String, Option[String])]] = {
    for {
     courses <- userLessonHistoryRepo.getCourseNav
    } yield courses
  }

  def getById(id: UUID): NDAResult[Course] = {
    for {
      course <- OptionT(courseRepo.getById(id)).toRight(NDError("error.user.not_found", NOT_FOUND, Some("COURSE_NOT_FOUND")))
    } yield course
  }

  def updateFieldQuery(id: UUID) = {
    courseRepo.table.filter(_.id === id)
  }

  def edit(id: UUID, name: Option[String], introduction: String) = {
    val updateQueries = DBIO.seq(
        name.map(f => updateFieldQuery(id).map(_.name).update(f)).getOrElse(DBIO.successful(0)),
        updateFieldQuery(id).map(_.introduction).update(Some(introduction)),
        (updateFieldQuery(id).map(_.modifiedAt).update(Instant.now))
      ).transactionally

      for {
        _ <- db.run(updateQueries) 
      } yield ()
  }
  def deleteById(id: UUID) = {
    for {
      _ <- courseRepo.delete(id)
      _ <- sectionRepo.updateCourseToNone(id)
      _ <- lessonRepo.updateCourseToNone(id)
    } yield ()
  }
}