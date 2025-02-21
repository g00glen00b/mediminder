import {Period} from './period';

export interface CreateScheduleRequest {
  medicationId: string;
  interval: string;
  period: Period;
  time: string;
  description?: string;
  dose: number;
}
