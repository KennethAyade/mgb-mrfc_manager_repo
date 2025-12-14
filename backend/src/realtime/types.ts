export type MeetingEventType =
  | 'CONNECTED'
  | 'AGENDA_ITEM_HIGHLIGHT_CHANGED';

export interface MeetingEvent<TPayload = any> {
  type: MeetingEventType;
  agendaId: number;
  itemId?: number;
  payload?: TPayload;
  ts: string; // ISO timestamp
}

export interface AgendaItemHighlightChangedPayload {
  isHighlighted: boolean;
  isOtherMatter: boolean;
}
