import {Color} from '../../shared/models/color';

export interface CreateMedicationRequest {
  name: string;
  medicationTypeId: string;
  administrationTypeId: string;
  doseTypeId: string;
  dosesPerPackage: number;
  color: Color;
}
