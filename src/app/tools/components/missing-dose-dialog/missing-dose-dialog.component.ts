import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {DoseMatch} from "../../models/dose-match";

@Component({
  selector: 'mediminder-missing-dose-dialog',
  templateUrl: './missing-dose-dialog.component.html',
  styleUrls: ['./missing-dose-dialog.component.scss']
})
export class MissingDoseDialogComponent implements OnInit {
  missingPrescriptions: number = 0;

  constructor(
    public dialogRef: MatDialogRef<MissingDoseDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public match: DoseMatch) {
  }

  ngOnInit(): void {
    this.missingPrescriptions = Math.ceil((this.match.requiredDoses - this.match.availableDoses) / this.match.averageInitialDose);
  }
}
