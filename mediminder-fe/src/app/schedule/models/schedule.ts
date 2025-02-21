import {Medication} from '../../medication/models/medication';
import {Period} from './period';

export interface Schedule {
  id: string;
  medication: Medication;
  interval: string;
  period: Period;
  description?: string;
  dose: number;
  time: string;
}
