package aecornext.schedule

import aecornext.data.Composer
import aecornext.encoding.{ KeyDecoder, KeyEncoder }

final case class ScheduleBucketId(scheduleName: String, scheduleBucket: String)

object ScheduleBucketId {
  val encoder = Composer.WithSeparator('-')

  implicit val keyEncoder: KeyEncoder[ScheduleBucketId] = KeyEncoder.instance[ScheduleBucketId] {
    case ScheduleBucketId(scheduleName, scheduleBucket) =>
      encoder(scheduleName, scheduleBucket)
  }
  implicit val keyDecoder: KeyDecoder[ScheduleBucketId] =
    KeyDecoder[String].collect {
      case encoder(scheduleName :: scheduleBucket :: Nil) =>
        ScheduleBucketId(scheduleName, scheduleBucket)
    }

}
