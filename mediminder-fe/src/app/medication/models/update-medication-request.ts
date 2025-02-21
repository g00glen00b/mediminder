import {Color} from '../../shared/models/color';

export interface UpdateMedicationRequest {
  name: string;
  administrationTypeId: string;
  doseTypeId: string;
  dosesPerPackage: number;
  color: Color;
}
