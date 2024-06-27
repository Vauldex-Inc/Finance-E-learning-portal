package tech.vauldex
package models.domain

import java.util.UUID
import java.time.{ Instant, ZoneId }
import java.time.format.DateTimeFormatter
import play.api.libs.json._
import play.api.libs.functional.syntax._
import models.domain._


case class Lesson(
    lessonId: UUID,
    isLearned: Boolean,
    title: String,
    createdAt: Instant,
    modifiedAt: Instant,
    content: Option[String] = None,
    url: Option[String] = None,
    order: Option[Int] = None,
    sectionId: Option[UUID] = None,
    courseId : Option[UUID] = None,
    thumbnail: Option[String] = None,
    introduction: Option[String] = None)

object Lesson {
  implicit val format: Format[Lesson] = Json.format[Lesson]
}

case class SectionsWithLessons(section: Section, lesson: Lesson)

object SectionsWithLessons {
  implicit val format: Format[SectionsWithLessons] = Json.format[SectionsWithLessons]
}

case class LessonWithCourse(
    lessonId: UUID,
    isLearned: Boolean,
    title: String,
    createdAt: Instant,
    modifiedAt: Instant,
    content: Option[String],
    url: Option[String],
    order: Option[Int] = None,
    sectionId: Option[UUID],
    introduction: Option[String],
    courseId: Option[UUID],
    courseTitle: Option[String]
)

object LessonWithCourse {

  def formatter(instant: Instant) = {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault())
    formatter.format(instant)
  }

  implicit val writes: Writes[LessonWithCourse] = (lesson: LessonWithCourse) => {
    Json.obj(
      "id" -> lesson.lessonId,
      "isLearned" -> lesson.isLearned,
      "title" -> lesson.title,
      "createdAt" -> formatter(lesson.createdAt),
      "modifiedAt" -> formatter(lesson.modifiedAt),
      "content" -> lesson.content,
      "url" -> lesson.url,
      "order" -> lesson.order,
      "sectionId" -> lesson.sectionId,
      "introduction" -> lesson.introduction,
      "courseId" -> lesson.courseId,
      "courseTitle" -> lesson.courseTitle
    )
  }
}

case class LessonWithHistory(
    lessonId: UUID,
    isLearned: Boolean,
    title: String,
    createdAt: Instant,
    modifiedAt: Instant,
    content: Option[String],
    url: Option[String],
    order: Option[Int] = None,
    sectionId: Option[UUID],
    introduction: Option[String],
    thumbnail: Option[String] = None,
    status: Boolean = false)

object LessonWithHistory {

  def formatter(instant: Instant) = {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault())
    formatter.format(instant)
  }

  implicit val writes: Writes[LessonWithHistory] = (lesson: LessonWithHistory) => {
    val baseJson = Json.obj(
      "id" -> lesson.lessonId,
      "isLearned" -> lesson.isLearned,
      "introduction" -> lesson.introduction,
      "url" -> lesson.url,
      "order" -> lesson.order,
      "title" -> lesson.title,
      "createdAt" -> formatter(lesson.createdAt),
      "modifiedAt" -> formatter(lesson.modifiedAt),
      "thumbnail" -> lesson.thumbnail,
      "status" -> lesson.status
    )

    val contentJson = lesson.content.map(content => Json.obj("content" -> content)).getOrElse(Json.obj())
    val sectionIdJson = lesson.sectionId.map(sectionId => Json.obj("sectionId" -> sectionId)).getOrElse(Json.obj())

    baseJson ++ contentJson ++ sectionIdJson
  }
}
