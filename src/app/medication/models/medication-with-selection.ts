import {Medication} from "./medication";

export interface MedicationWithSelection {
  medication: Medication;
  selected: boolean;
}
