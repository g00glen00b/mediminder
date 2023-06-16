import {Component, Inject} from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from "@angular/material/dialog";
import {Schedule} from "../../models/schedule";
import { ScheduleRecurrenceTypePipe } from '../../pipes/schedule-recurrence-type.pipe';
import { NgIf, DatePipe } from '@angular/common';
import { MatListModule } from '@angular/material/list';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatToolbarModule } from '@angular/material/toolbar';

@Component({
    selector: 'mediminder-schedule-dialog',
    templateUrl: './schedule-dialog.component.html',
    styleUrls: ['./schedule-dialog.component.scss'],
    standalone: true,
    imports: [MatToolbarModule, MatButtonModule, MatIconModule, MatDialogModule, MatDividerModule, MatListModule, NgIf, DatePipe, ScheduleRecurrenceTypePipe]
})
export class ScheduleDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<ScheduleDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public schedule: Schedule) {
  }
}
