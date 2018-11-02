package aecornext.schedule.process

import aecornext.data.EntityEvent
import aecornext.schedule.{ ScheduleBucketId, ScheduleEvent }

trait ScheduleEventJournal[F[_]] {
  def processNewEvents(f: EntityEvent[ScheduleBucketId, ScheduleEvent] => F[Unit]): F[Unit]
}
