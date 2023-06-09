import {Medication} from "../../medication/models/medication";
import {SchedulePeriod} from "./schedule-period";
import {ScheduleRecurrence} from "./schedule-recurrence";

export interface Schedule {
  id: string;
  medication: Medication;
  dose: number;
  period: SchedulePeriod;
  recurrence: ScheduleRecurrence;
  time: string;
  description?: string;
}
