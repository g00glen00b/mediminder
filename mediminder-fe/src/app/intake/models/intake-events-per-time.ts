import {IntakeEvent} from './intake-event';
import {format} from 'date-fns';

export interface IntakeEventsPerTime {
  events: IntakeEvent[];
  time: string;
}

export function groupPerTime(events: IntakeEvent[]): IntakeEventsPerTime[] {
  const groups: Map<string, IntakeEvent[]> = events.reduce((entryMap, event) => {
    const time: string = format(event.targetDate, 'HH:mm');
    const existingEventsAtTime = entryMap.get(time) || [];
    return entryMap.set(time, [...existingEventsAtTime, event]);
  }, new Map());
  return Array.from(groups, ([time, events]) => ({time, events}));
}
