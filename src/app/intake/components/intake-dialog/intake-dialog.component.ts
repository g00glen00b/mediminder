import {Component, inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from "@angular/material/dialog";
import {Intake} from "../../models/intake";
import {ScheduleRecurrenceTypePipe} from '../../../schedule/pipes/schedule-recurrence-type.pipe';
import {DatePipe, NgIf} from '@angular/common';
import {MatListModule} from '@angular/material/list';
import {MatDividerModule} from '@angular/material/divider';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatToolbarModule} from '@angular/material/toolbar';

@Component({
  selector: 'mediminder-intake-dialog',
  templateUrl: './intake-dialog.component.html',
  styleUrls: ['./intake-dialog.component.scss'],
  standalone: true,
  imports: [
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    MatDividerModule,
    MatListModule,
    NgIf,
    DatePipe,
    ScheduleRecurrenceTypePipe
  ]
})
export class IntakeDialogComponent {
  public dialogRef = inject(MatDialogRef<IntakeDialogComponent>);
  public intake: Intake = inject(MAT_DIALOG_DATA);
}
