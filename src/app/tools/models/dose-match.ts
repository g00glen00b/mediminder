import {Medication} from "../../medication/models/medication";

export interface DoseMatch {
  requiredDoses: number;
  availableDoses: number;
  averageInitialDose: number;
  medication: Medication;
}
