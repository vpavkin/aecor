package aecornext.akka.persistence.cassandra

import akka.Done
import akka.actor.{ActorSystem, ExtendedActorSystem}
import akka.event.Logging
import Session.Init
import akka.persistence.cassandra.session.CassandraSessionSettings
import akka.persistence.cassandra.session.scaladsl.CassandraSession
import cats.effect.Effect
import cats.implicits._
import aecornext.util.effect._
import akka.persistence.cassandra.SessionProvider

object DefaultJournalCassandraSession {

  /**
    * Creates CassandraSession using settings of default cassandra journal.
    *
    */
  def apply[F[_]](system: ActorSystem, metricsCategory: String, init: Init[F])(
    implicit F: Effect[F]
  ): F[CassandraSession] = F.delay {
    val log = Logging(system, classOf[CassandraSession])
    val provider = SessionProvider(
      system.asInstanceOf[ExtendedActorSystem],
      system.settings.config.getConfig("cassandra-journal")
    )
    val settings = CassandraSessionSettings(system.settings.config.getConfig("cassandra-journal"))
    new CassandraSession(system, provider, settings, system.dispatcher, log, metricsCategory, { x =>
      init(Session[F](x)).as(Done).toIO.unsafeToFuture()
    })
  }
}