import {Component, inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from "@angular/material/dialog";
import {CabinetEntry} from "../../models/cabinet-entry";
import {DatePipe, DecimalPipe} from '@angular/common';
import {MatListModule} from '@angular/material/list';
import {MatDividerModule} from '@angular/material/divider';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatToolbarModule} from '@angular/material/toolbar';

@Component({
  selector: 'mediminder-cabinet-entry-dialog',
  templateUrl: './cabinet-entry-dialog.component.html',
  styleUrls: ['./cabinet-entry-dialog.component.scss'],
  standalone: true,
  imports: [
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    MatDividerModule,
    MatListModule,
    DecimalPipe,
    DatePipe
  ]
})
export class CabinetEntryDialogComponent {
    public dialogRef = inject(MatDialogRef<CabinetEntryDialogComponent>);
    public entry: CabinetEntry = inject(MAT_DIALOG_DATA);
}
