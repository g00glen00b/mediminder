import {ScheduleRecurrenceType} from "./schedule-recurrence-type";

export interface ScheduleRecurrenceTypeWrapper {
  type: ScheduleRecurrenceType;
  label: string;
}

export const RECURRENCE_TYPES: ScheduleRecurrenceTypeWrapper[] = [
  {type: 'daily', label: 'Day(s)'},
  {type: 'weekly', label: 'Week(s)'},
];
