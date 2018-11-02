package aecornext.example.transaction
import aecornext.runtime.Eventsourced.Entities

package object transaction {
  type Transactions[F[_]] = Entities.Rejectable[TransactionId, Algebra, F, String]
}
