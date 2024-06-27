package tech.vauldex
package models.domain

import java.util.UUID
import java.time.Instant
import play.api.libs.json._

case class Section(
    sectionId: UUID,
    name: String,
    courseId: Option[UUID],
    order: Option[Int] = None)

object Section {
    implicit val format: Format[Section] = Json.format[Section]
}
