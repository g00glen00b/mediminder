import {Medication} from '../../medication/models/medication';

export interface IntakeEvent {
  id?: string;
  scheduleId: string;
  medication: Medication;
  targetDate: string;
  completedDate?: string;
  dose: number;
  description?: string;
}
