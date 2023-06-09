import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {Medication} from "../../models/medication";

@Component({
  selector: 'mediminder-medication-dialog',
  templateUrl: './medication-dialog.component.html',
  styleUrls: ['./medication-dialog.component.scss']
})
export class MedicationDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<MedicationDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public medication: Medication) {
  }
}
