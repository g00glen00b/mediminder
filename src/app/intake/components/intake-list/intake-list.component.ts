import {Component, EventEmitter, Input, OnChanges, Output, SimpleChanges} from '@angular/core';
import {Intake} from "../../models/intake";
import {IntakePerTime} from "../../models/intake-per-time";
import {format} from "date-fns";
import {IntakeDialogComponent} from "../intake-dialog/intake-dialog.component";
import {MatDialog} from "@angular/material/dialog";

@Component({
  selector: 'mediminder-intake-list',
  templateUrl: './intake-list.component.html',
  styleUrls: ['./intake-list.component.scss']
})
export class IntakeListComponent implements OnChanges {
  @Input()
  intakes: Intake[] = [];
  @Output()
  complete: EventEmitter<Intake> = new EventEmitter<Intake>();
  @Output()
  openSchedule: EventEmitter<Intake> = new EventEmitter<Intake>();
  intakesPerTime: IntakePerTime[] = [];

  constructor(private dialog: MatDialog) {
  }

  ngOnChanges(changes: SimpleChanges): void {
    const groups: Map<string, Intake[]> = this.intakes.reduce((entryMap, intake) => {
      const time: string = format(intake.scheduledDate, 'HH:mm');
      const existingIntakesAtTime: Intake[] = entryMap.get(time) || [];
      return entryMap.set(time, [...existingIntakesAtTime, intake]);
    }, new Map());
    this.intakesPerTime = Array.from(groups, ([time, intakes]) => ({time, intakes}));
  }

  onSelect(intake: Intake): void {
    this.dialog
      .open(IntakeDialogComponent, {data: intake, height: '100vh', width: '100vw', maxWidth: '100vw', maxHeight: '100vh'})
      .afterClosed()
      .subscribe(event => {
        if (event === 'complete') this.complete.emit(intake);
        if (event === 'openSchedule') this.openSchedule.emit(intake);
      });
  }
}
