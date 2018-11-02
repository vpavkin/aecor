package aecornext.example.account

import aecornext.data.Enriched
import aecornext.example.common.{Amount, Timestamp}
import aecornext.example.persistentEncoderUtil
import io.circe.generic.auto._
import io.circe.java8.time._
import aecornext.runtime.akkapersistence.serialization.{PersistentDecoder, PersistentEncoder}
import io.circe.Encoder

sealed abstract class AccountEvent extends Product with Serializable

object AccountEvent {
  case class AccountOpened(checkBalance: Boolean) extends AccountEvent

  case class AccountDebited(transactionId: AccountTransactionId, amount: Amount)
      extends AccountEvent

  case class AccountCredited(transactionId: AccountTransactionId, amount: Amount)
      extends AccountEvent

  implicit val encoder: PersistentEncoder[Enriched[Timestamp, AccountEvent]] =
    persistentEncoderUtil.circePersistentEncoder(Encoder[Enriched[Timestamp, AccountEvent]])

  implicit val decoder: PersistentDecoder[Enriched[Timestamp, AccountEvent]] =
    persistentEncoderUtil.circePersistentDecoder[Enriched[Timestamp, AccountEvent]]
}
