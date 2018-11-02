package aecornext.example.transaction
import java.util.UUID

import aecornext.example.common.Timestamp
import aecornext.example.transaction.transaction.Transactions
import aecornext.runtime.Eventsourced
import aecornext.runtime.akkapersistence.AkkaPersistenceRuntime
import aecornext.util.Clock
import cats.implicits._
import cats.effect.Effect
import scodec.codecs.implicits._

object deployment {
  def deploy[F[_]: Effect](runtime: AkkaPersistenceRuntime[UUID],
                           clock: Clock[F]): F[Transactions[F]] =
    runtime
      .deploy(
        "Transaction",
        EventsourcedAlgebra.behavior[F].enrich(clock.instant.map(Timestamp(_))),
        EventsourcedAlgebra.tagging
      )
      .map(Eventsourced.Entities.fromEitherK(_))
}
