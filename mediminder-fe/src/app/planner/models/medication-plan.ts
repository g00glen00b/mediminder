import {Medication} from '../../medication/models/medication';

export interface MedicationPlan {
  medication: Medication;
  availableDoses: number;
  requiredDoses: number;
}
