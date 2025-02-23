import {MedicationType} from './medication-type';
import {DoseType} from './dose-type';
import {AdministrationType} from './administration-type';
import {Color} from '../../shared/models/color';

export interface Medication {
  id: string;
  name: string;
  medicationType: MedicationType;
  doseType: DoseType;
  administrationType: AdministrationType;
  dosesPerPackage: number;
  color: Color;
}

export function getMedicationLabel(medication?: string | Medication): string {
  if (medication == null) return '';
  if (typeof medication == 'string') return medication;
  return medication.name;
}
