import type { Response } from 'express';
import type { MeetingEvent } from './types';

// Per-instance connection bookkeeping. Cross-instance broadcast is done via Redis.
class SseHub {
  private clientsByAgendaId: Map<number, Set<Response>> = new Map();

  addClient(agendaId: number, res: Response): void {
    const set = this.clientsByAgendaId.get(agendaId) ?? new Set<Response>();
    set.add(res);
    this.clientsByAgendaId.set(agendaId, set);
  }

  removeClient(agendaId: number, res: Response): void {
    const set = this.clientsByAgendaId.get(agendaId);
    if (!set) return;
    set.delete(res);
    if (set.size === 0) this.clientsByAgendaId.delete(agendaId);
  }

  broadcastToAgenda(agendaId: number, evt: MeetingEvent): void {
    const set = this.clientsByAgendaId.get(agendaId);
    if (!set || set.size === 0) return;

    const data = JSON.stringify(evt);
    // Use an explicit event name for easier debugging on clients.
    const ssePayload = `event: ${evt.type}\n` + `data: ${data}\n\n`;

    for (const res of set) {
      try {
        res.write(ssePayload);
      } catch {
        // If write fails, drop client.
        this.removeClient(agendaId, res);
      }
    }
  }

  getClientCount(agendaId: number): number {
    return this.clientsByAgendaId.get(agendaId)?.size ?? 0;
  }
}

export const sseHub = new SseHub();
