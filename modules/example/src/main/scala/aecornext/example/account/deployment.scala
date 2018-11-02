package aecornext.example.account
import java.util.UUID

import aecornext.example.common.Timestamp
import aecornext.runtime.Eventsourced
import aecornext.runtime.akkapersistence.AkkaPersistenceRuntime
import aecornext.util.Clock
import cats.effect.Effect
import cats.implicits._

object deployment {
  def deploy[F[_]: Effect](runtime: AkkaPersistenceRuntime[UUID], clock: Clock[F]): F[Accounts[F]] =
    runtime
      .deploy(
        "Account",
        EventsourcedAlgebra.behavior[F].enrich(clock.instant.map(Timestamp(_))),
        EventsourcedAlgebra.tagging
      )
      .map(Eventsourced.Entities.fromEitherK(_))
}
