import {Period} from './period';

export interface UpdateScheduleRequest {
  interval: string;
  period: Period;
  time: string;
  description?: string;
  dose: number;
}
