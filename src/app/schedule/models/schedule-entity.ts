import {SchedulePeriod} from "./schedule-period";
import {ScheduleRecurrence} from "./schedule-recurrence";

export interface ScheduleEntity {
  id: string;
  medicationId: string;
  dose: number;
  period: SchedulePeriod;
  recurrence: ScheduleRecurrence;
  time: string;
  description?: string;
}
