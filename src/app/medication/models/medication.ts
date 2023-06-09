import {MedicationType} from "./medication-type";

export interface Medication {
  id: string;
  name: string;
  type: MedicationType;
}
