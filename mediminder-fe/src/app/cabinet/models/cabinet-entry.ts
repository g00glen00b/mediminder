import {Medication} from '../../medication/models/medication';

export interface CabinetEntry {
  id: string;
  medication: Medication;
  remainingDoses: number;
  expiryDate: string;
}
