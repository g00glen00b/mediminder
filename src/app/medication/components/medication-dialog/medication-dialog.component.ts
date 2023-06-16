import {Component, Inject} from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from "@angular/material/dialog";
import {Medication} from "../../models/medication";
import { MatListModule } from '@angular/material/list';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatToolbarModule } from '@angular/material/toolbar';

@Component({
  selector: 'mediminder-medication-dialog',
  templateUrl: './medication-dialog.component.html',
  styleUrls: ['./medication-dialog.component.scss'],
  standalone: true,
  imports: [
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    MatDividerModule,
    MatListModule
  ]
})
export class MedicationDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<MedicationDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public medication: Medication) {
  }
}
