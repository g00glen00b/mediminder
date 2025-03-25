import {Medication} from '../../medication/models/medication';

export interface Document {
  id: string;
  filename: string;
  expiryDate?: string;
  relatedMedication?: Medication;
  description?: string;
}
