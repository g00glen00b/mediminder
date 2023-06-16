import {Component, EventEmitter, inject, Input, Output} from '@angular/core';
import {Schedule} from "../../models/schedule";
import {MatDialog} from "@angular/material/dialog";
import {ScheduleDialogComponent} from "../schedule-dialog/schedule-dialog.component";
import {ScheduleRecurrenceTypePipe} from '../../pipes/schedule-recurrence-type.pipe';
import {EmptyStateComponent} from '../../../shared/components/empty-state/empty-state.component';
import {DatePipe, NgFor, NgIf} from '@angular/common';
import {MatListModule} from '@angular/material/list';

@Component({
  selector: 'mediminder-schedule-list',
  templateUrl: './schedule-list.component.html',
  styleUrls: ['./schedule-list.component.scss'],
  standalone: true,
  imports: [
    MatListModule,
    NgFor,
    NgIf,
    EmptyStateComponent,
    DatePipe,
    ScheduleRecurrenceTypePipe
  ]
})
export class ScheduleListComponent {
  @Input()
  entries: Schedule[] = [];
  @Output()
  delete: EventEmitter<Schedule> = new EventEmitter<Schedule>();
  @Output()
  edit: EventEmitter<Schedule> = new EventEmitter<Schedule>();
  @Output()
  copy: EventEmitter<Schedule> = new EventEmitter<Schedule>();
  private dialog = inject(MatDialog);

  onClickItem(schedule: Schedule) {
    this.dialog
      .open(ScheduleDialogComponent, {data: schedule, height: '100vh', width: '100vw', maxWidth: '100vw', maxHeight: '100vh'})
      .afterClosed()
      .subscribe(event => {
        if (event === 'delete') this.delete.emit(schedule);
        if (event === 'edit') this.edit.emit(schedule);
        if (event === 'copy') this.copy.emit(schedule);
      })
  }
}
