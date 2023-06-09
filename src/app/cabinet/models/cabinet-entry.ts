import {Medication} from "../../medication/models/medication";

export interface CabinetEntry {
  id: string;
  medication: Medication;
  initialUnits: number;
  units: number;
  expiryDate: Date;
}
