import {MediminderEvent} from "../../shared/models/mediminder-event";

export class IntakeCompletedEvent implements MediminderEvent {
  constructor(public medicationId: string, public dose: number) {
  }
}
