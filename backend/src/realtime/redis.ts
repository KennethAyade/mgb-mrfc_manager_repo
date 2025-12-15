import IORedis from 'ioredis';
import type { MeetingEvent } from './types';

const CHANNEL = process.env.REALTIME_CHANNEL || 'meeting-events';
const REDIS_URL = process.env.REDIS_URL;
const NODE_ENV = process.env.NODE_ENV || 'development';

// In production we require Redis (multi-instance fanout).
// In local development we tolerate Redis being missing/unreachable.
const STRICT = NODE_ENV === 'production' || process.env.REALTIME_STRICT === 'true';

export interface RealtimeRedis {
  publishMeetingEvent: (evt: MeetingEvent) => Promise<void>;
  startSubscriber: (onEvent: (evt: MeetingEvent) => void) => Promise<void>;
  stop: () => Promise<void>;
  isEnabled: () => boolean;
}

let publisher: IORedis | null = null;
let subscriber: IORedis | null = null;
let enabled = false;
let warned = false;

const warnOnce = (msg: string, err?: any) => {
  if (warned) return;
  warned = true;
  console.warn(`🟡 Realtime Redis disabled: ${msg}`);
  if (err?.message) console.warn(err.message);
};

const disable = async (msg: string, err?: any) => {
  warnOnce(msg, err);
  try {
    if (subscriber) subscriber.disconnect();
    if (publisher) publisher.disconnect();
  } catch {
    // ignore
  }
  subscriber = null;
  publisher = null;
  enabled = false;
};

const ensureClients = (): void => {
  if (enabled) return;

  if (!REDIS_URL) {
    if (STRICT) {
      throw new Error('REDIS_URL is required for REALTIME in production');
    }
    return;
  }

  // IMPORTANT:
  // - maxRetriesPerRequest: null avoids MaxRetriesPerRequestError crashing the app.
  // - keepRetrying by default; we'll handle errors and disable in dev if unreachable.
  publisher = new IORedis(REDIS_URL, { maxRetriesPerRequest: null });
  subscriber = new IORedis(REDIS_URL, { maxRetriesPerRequest: null });

  // Prevent unhandled error events from crashing the process.
  publisher.on('error', (err) => {
    if (!STRICT) void disable('publisher connection error', err);
  });
  subscriber.on('error', (err) => {
    if (!STRICT) void disable('subscriber connection error', err);
  });

  enabled = true;
};

export const realtimeRedis: RealtimeRedis = {
  isEnabled: () => enabled,

  publishMeetingEvent: async (evt: MeetingEvent) => {
    ensureClients();
    if (!enabled || !publisher) return;

    try {
      await publisher.publish(CHANNEL, JSON.stringify(evt));
    } catch (err: any) {
      if (STRICT) throw err;
      await disable('publish failed', err);
    }
  },

  startSubscriber: async (onEvent: (evt: MeetingEvent) => void) => {
    ensureClients();
    if (!enabled || !subscriber) return;

    try {
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
    } catch (err: any) {
      if (STRICT) throw err;
      await disable('subscribe failed (is Redis reachable?)', err);
    }
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
