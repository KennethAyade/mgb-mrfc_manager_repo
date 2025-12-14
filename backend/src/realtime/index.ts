import { realtimeRedis } from './redis';
import { sseHub } from './sseHub';
import type { MeetingEvent } from './types';

const REALTIME_ENABLED = process.env.REALTIME_ENABLED !== 'false';

export const isRealtimeEnabled = (): boolean => REALTIME_ENABLED;

export const initRealtime = async (): Promise<void> => {
  if (!REALTIME_ENABLED) {
    console.log('🟡 Realtime disabled (REALTIME_ENABLED=false)');
    return;
  }

  // Start Redis subscriber (if REDIS_URL is configured)
  await realtimeRedis.startSubscriber((evt: MeetingEvent) => {
    // Fan-out to local SSE clients for this agendaId
    if (typeof evt.agendaId === 'number') {
      sseHub.broadcastToAgenda(evt.agendaId, evt);
    }
  });

  if (realtimeRedis.isEnabled()) {
    console.log('✅ Realtime enabled (Redis Pub/Sub + SSE)');
  } else {
    console.log('🟡 Realtime not active (REDIS_URL not configured)');
  }
};

export const publishMeetingEvent = async (evt: MeetingEvent): Promise<void> => {
  if (!REALTIME_ENABLED) return;

  // If Redis is configured, publish for cross-instance delivery.
  // If not configured, do a best-effort local broadcast (dev convenience).
  if (realtimeRedis.isEnabled()) {
    await realtimeRedis.publishMeetingEvent(evt);
  } else {
    sseHub.broadcastToAgenda(evt.agendaId, evt);
  }
};
