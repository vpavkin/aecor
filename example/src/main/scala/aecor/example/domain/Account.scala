package aecor.example.domain

import aecor.core.entity.CommandHandlerResult._
import aecor.core.entity._
import aecor.core.message.Correlation
import io.circe.{Decoder, Encoder}
import io.circe.generic.auto._

case class AccountId(value: String) extends AnyVal
object Account {
  sealed trait Event {
    def accountId: AccountId
  }
  case class AccountOpened(accountId: AccountId) extends Event
  case class HoldPlaced(accountId: AccountId, transactionId: TransactionId, amount: Amount) extends Event
  case class HoldCancelled(accountId: AccountId, transactionId: TransactionId) extends Event
  case class HoldCleared(accountId: AccountId, transactionId: TransactionId, amount: Amount) extends Event
  case class AccountCredited(accountId: AccountId, transactionId: TransactionId, amount: Amount) extends Event

  implicit val eventEncoder: Encoder[Event] = shapeless.cachedImplicit
  implicit val eventDecoder: Decoder[Event] = shapeless.cachedImplicit

  sealed trait Command {
    def accountId: AccountId
  }
  case class OpenAccount(accountId: AccountId) extends Command
  case class PlaceHold(accountId: AccountId, transactionId: TransactionId, amount: Amount) extends Command
  case class CancelHold(accountId: AccountId, transactionId: TransactionId) extends Command
  case class ClearHold(accountId: AccountId, transactionId: TransactionId, amount: Amount) extends Command
  case class CreditAccount(accountId: AccountId, transactionId: TransactionId, amount: Amount) extends Command

  sealed trait Rejection
  case object AccountDoesNotExist extends Rejection
  case object InsufficientFunds extends Rejection
  case object AccountExists extends Rejection
  case object HoldNotFound extends Rejection

  sealed trait State
  object State
  case object Initial extends State
  case class Open(id: AccountId, balance: Amount, holds: Map[TransactionId, Amount]) extends State

  implicit def correlation: Correlation[Command] =
    Correlation.instance(_.accountId.value)

  implicit val entityName: EntityName[Account] =
    EntityName.instance("Account")

  implicit val commandContract: CommandContract.Aux[Account, Command, Rejection] =
    CommandContract.instance

  implicit def behavior: EntityBehavior[Account, State, Command, Event, Rejection] = new EntityBehavior[Account, State, Command, Event, Rejection] {
    override def initialState(entity: Account): State = Initial

    override def commandHandler(entity: Account): CommandHandler[State, Command, Event, Rejection] = CommandHandler.instance {
      case Initial => {
        case OpenAccount(accountId) => accept(AccountOpened(accountId))
        case _ => reject(AccountDoesNotExist)
      }
      case Open(id, balance, holds) => {
        case c: OpenAccount =>
          reject(AccountExists)

        case PlaceHold(_, transactionId, amount) =>
          if (balance > amount) {
            accept(HoldPlaced(id, transactionId, amount))
          } else {
            reject(InsufficientFunds)
          }

        case CancelHold(_, transactionId) =>
          accept(HoldCancelled(id, transactionId))

        case ClearHold(_, transactionId, clearingAmount) =>
          holds.get(transactionId) match {
            case Some(amount) =>
              accept(HoldCleared(id, transactionId, clearingAmount))
            case None =>
              reject(HoldNotFound)
          }

        case CreditAccount(_, transactionId, amount) =>
          accept(AccountCredited(id, transactionId, amount))
      }
    }

    override def eventProjector(entity: Account): EventProjector[State, Event] = EventProjector.instance {
      case Initial => {
        case AccountOpened(accountId) => Open(accountId, Amount(0), Map.empty)
        case other => throw new IllegalArgumentException(s"Unexpected event $other")
      }
      case self @ Open(id, balance, holds) => {
        case e: AccountOpened => self
        case e: HoldPlaced => self.copy(holds = holds + (e.transactionId -> e.amount), balance = balance - e.amount)
        case e: HoldCancelled => holds.get(e.transactionId).map(holdAmount => self.copy(holds = holds - e.transactionId, balance = balance + holdAmount)).getOrElse(self)
        case e: AccountCredited => self.copy(balance = balance + e.amount)
        case e: HoldCleared => self.copy(holds = holds - e.transactionId)
      }
    }
  }

  def apply(): Account = new Account {}

}

sealed trait Account