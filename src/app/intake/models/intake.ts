import {Schedule} from "../../schedule/models/schedule";
import {CompletedIntake} from "./completed-intake";

export interface Intake {
  schedule: Schedule;
  scheduledDate: Date;
  completed: CompletedIntake | null;
}
