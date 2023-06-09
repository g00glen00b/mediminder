import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {CabinetEntry} from "../../models/cabinet-entry";

@Component({
  selector: 'mediminder-cabinet-entry-dialog',
  templateUrl: './cabinet-entry-dialog.component.html',
  styleUrls: ['./cabinet-entry-dialog.component.scss']
})
export class CabinetEntryDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<CabinetEntryDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public entry: CabinetEntry) {
  }
}
