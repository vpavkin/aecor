package aecornext.example
import aecornext.runtime.Eventsourced.Entities

package object account {
  type Accounts[F[_]] = Entities.Rejectable[AccountId, Algebra, F, Rejection]
}
