import {Component, EventEmitter, Input, OnChanges, Output, SimpleChanges} from '@angular/core';
import {Intake} from "../../models/intake";
import {IntakePerTime} from "../../models/intake-per-time";
import {format} from "date-fns";
import {IntakeDialogComponent} from "../intake-dialog/intake-dialog.component";
import {MatDialog} from "@angular/material/dialog";
import {compareByField} from "../../../shared/utils/compare-utils";
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { NgFor, NgIf } from '@angular/common';
import { MatListModule } from '@angular/material/list';

@Component({
  selector: 'mediminder-intake-list',
  templateUrl: './intake-list.component.html',
  styleUrls: ['./intake-list.component.scss'],
  standalone: true,
  imports: [
    MatListModule,
    NgFor,
    NgIf,
    EmptyStateComponent
  ]
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
    this.intakesPerTime = Array.from(groups, ([time, intakes]) => ({time, intakes: intakes.sort(compareByField(intake => intake.schedule.medication.name))}));
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
