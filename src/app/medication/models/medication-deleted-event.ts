import {MediminderEvent} from "../../shared/models/mediminder-event";

export class MedicationDeletedEvent implements MediminderEvent {
  constructor(public medicationId: string) {
  }
}
