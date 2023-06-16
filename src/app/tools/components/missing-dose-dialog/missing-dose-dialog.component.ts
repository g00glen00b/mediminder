import {Component, inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from "@angular/material/dialog";
import {DoseMatch} from "../../models/dose-match";
import {DecimalPipe} from '@angular/common';
import {MatListModule} from '@angular/material/list';
import {MatDividerModule} from '@angular/material/divider';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatToolbarModule} from '@angular/material/toolbar';

@Component({
  selector: 'mediminder-missing-dose-dialog',
  templateUrl: './missing-dose-dialog.component.html',
  styleUrls: ['./missing-dose-dialog.component.scss'],
  standalone: true,
  imports: [
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    MatDividerModule,
    MatListModule,
    DecimalPipe
  ]
})
export class MissingDoseDialogComponent implements OnInit {
  missingPrescriptions: number = 0;
  public dialogRef = inject(MatDialogRef<MissingDoseDialogComponent>);
  public match: DoseMatch = inject(MAT_DIALOG_DATA);

  ngOnInit(): void {
    this.missingPrescriptions = Math.ceil((this.match.requiredDoses - this.match.availableDoses) / this.match.averageInitialDose);
  }
}
