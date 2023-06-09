import {ScheduleRecurrenceType} from "./schedule-recurrence-type";

export interface ScheduleRecurrence {
  type: ScheduleRecurrenceType;
  units: number;
}
