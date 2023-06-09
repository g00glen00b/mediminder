import {Component, EventEmitter, Input, Output} from '@angular/core';
import {Schedule} from "../../models/schedule";
import {MatDialog} from "@angular/material/dialog";
import {ScheduleDialogComponent} from "../schedule-dialog/schedule-dialog.component";

@Component({
  selector: 'mediminder-schedule-list',
  templateUrl: './schedule-list.component.html',
  styleUrls: ['./schedule-list.component.scss']
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

  constructor(private dialog: MatDialog) {
  }

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
