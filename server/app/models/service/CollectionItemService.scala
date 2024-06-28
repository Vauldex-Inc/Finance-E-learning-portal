package tech.vauldex
package models.service

import java.util.UUID
import javax.inject.{ Inject, Singleton }
import scala.concurrent.{ ExecutionContext, Future }
import play.api.http.Status._
import play.api.libs.json._
import play.api.mvc._
import play.api.Configuration
import cats.implicits._
import cats.data.{ OptionT, EitherT }
import tech.vauldex._
import models.domain._
import models.repo.CollectionItemRepo
import errors.NDError
import utils._

@Singleton
final class CollectionItemService @Inject()(
    s3Service: S3Service,
    config: Configuration,
    val collectionItemRepo: CollectionItemRepo
)(implicit protected val ec: ExecutionContext){
  import CollectionItemRarity._
  lazy val iconCollectionImagePath = config.get[String]("aws.s3.old.path.collection.icon")
  lazy val largeCollectionImagePath = config.get[String]("aws.s3.old.path.collection.large")

  def create(collectionItem: CollectionItem): NDAResult[Unit] = {
    for {
      _ <- ioActionResult(collectionItemRepo.create(collectionItem), NDError("error.bad_request", NOT_FOUND, Some("BAD_REQUEST")), ())
    } yield ()
  }

  def getAll = {
    collectionItemRepo.all.flatMap{ collection => 
      Future.sequence(collection.map(collectionItem => {
        for {
          iconUrl <- s3Service.createPresignedGetUrl(collectionItem.unlockedImgIcon, iconCollectionImagePath).toOption.value
          largeImgUrl <- s3Service.createPresignedGetUrl(collectionItem.unlockedImgLarge, largeCollectionImagePath).toOption.value
        } yield (collectionItem, iconUrl, largeImgUrl)
      }))
    }
  }

  def update( id: UUID,
              name: Option[String], 
              title: Option[String],
              tooltip: Option[String],
              rarity: Option[CollectionItemRarity],
              unlockedImgIcon: Option[String],
              unlockedImgLarge: Option[String],
              order: Option[Int]
              ): NDAResult[Unit] = {
    for {
      collectionItem <- OptionT(collectionItemRepo.getById(id)).toRight(NDError("error.collection_item.not_found", NOT_FOUND, Some("COLLECTION_ITEM_NOT_FOUND")))
      _ <- EitherT.right(collectionItemRepo.update(collectionItem.id, name, title, tooltip,rarity, unlockedImgIcon, unlockedImgLarge, order))
    } yield ()
  }

  def deleteById(id: UUID): Future[Unit] = {
    for {
      _ <- collectionItemRepo.deleteById(id)
    } yield ()
  }

}



