import {TotalIntakeDose} from "../../intake/models/total-intake-dose";
import {TotalAvailableDose} from "../../cabinet/models/total-available-dose";

export interface DoseMatchTuple {
  required: TotalIntakeDose;
  available: TotalAvailableDose;
}
