import IORedis from 'ioredis';
import type { MeetingEvent } from './types';

const CHANNEL = process.env.REALTIME_CHANNEL || 'meeting-events';
const REDIS_URL = process.env.REDIS_URL;
const NODE_ENV = process.env.NODE_ENV || 'development';

export interface RealtimeRedis {
  publishMeetingEvent: (evt: MeetingEvent) => Promise<void>;
  startSubscriber: (onEvent: (evt: MeetingEvent) => void) => Promise<void>;
  stop: () => Promise<void>;
  isEnabled: () => boolean;
}

let publisher: IORedis | null = null;
let subscriber: IORedis | null = null;
let enabled = false;

const ensureClients = (): void => {
  if (enabled) return;

  if (!REDIS_URL) {
    // In dev, we allow startup without Redis (realtime will be disabled).
    // In production, we want Redis for cross-instance fanout.
    if (NODE_ENV === 'production') {
      throw new Error('REDIS_URL is required for REALTIME in production');
    }
    return;
  }

  publisher = new IORedis(REDIS_URL);
  subscriber = new IORedis(REDIS_URL);
  enabled = true;
};

export const realtimeRedis: RealtimeRedis = {
  isEnabled: () => enabled,

  publishMeetingEvent: async (evt: MeetingEvent) => {
    ensureClients();
    if (!enabled || !publisher) return;
    await publisher.publish(CHANNEL, JSON.stringify(evt));
  },

  startSubscriber: async (onEvent: (evt: MeetingEvent) => void) => {
    ensureClients();
    if (!enabled || !subscriber) return;

    await subscriber.subscribe(CHANNEL);
    subscriber.on('message', (_channel, message) => {
      try {
        const evt = JSON.parse(message) as MeetingEvent;
        if (!evt || typeof evt.agendaId !== 'number' || typeof evt.type !== 'string') return;
        onEvent(evt);
      } catch {
        // ignore malformed messages
      }
    });
  },

  stop: async () => {
    const tasks: Promise<any>[] = [];
    if (subscriber) tasks.push(subscriber.quit());
    if (publisher) tasks.push(publisher.quit());
    await Promise.allSettled(tasks);
    subscriber = null;
    publisher = null;
    enabled = false;
  }
};
