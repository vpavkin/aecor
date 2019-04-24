package aecornext.runtime.akkageneric

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.cluster.sharding.ClusterShardingSettings

import scala.concurrent.duration._

final case class GenericAkkaRuntimeSettings(numberOfShards: Int,
                                            idleTimeout: FiniteDuration,
                                            askTimeout: FiniteDuration,
                                            clusterShardingSettings: ClusterShardingSettings)

object GenericAkkaRuntimeSettings {

  /**
    * Reads config from `aecornext.akka-runtime`, see reference.conf for details
    * @param system Actor system to get config from
    * @return default settings
    */
  def default(system: ActorSystem): GenericAkkaRuntimeSettings = {
    val config = system.settings.config.getConfig("aecornext.generic-akka-runtime")
    def getMillisDuration(path: String): FiniteDuration =
      Duration(config.getDuration(path, TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS)

    GenericAkkaRuntimeSettings(
      config.getInt("number-of-shards"),
      getMillisDuration("idle-timeout"),
      getMillisDuration("ask-timeout"),
      ClusterShardingSettings(system)
    )
  }
}