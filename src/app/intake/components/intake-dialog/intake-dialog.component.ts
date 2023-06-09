import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {Intake} from "../../models/intake";

@Component({
  selector: 'mediminder-intake-dialog',
  templateUrl: './intake-dialog.component.html',
  styleUrls: ['./intake-dialog.component.scss']
})
export class IntakeDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<IntakeDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public intake: Intake) {
  }

}
