import {Component, EventEmitter, Input, OnChanges, Output} from '@angular/core';
import {IntakeEvent} from '../../models/intake-event';
import {groupPerTime, IntakeEventsPerTime} from '../../models/intake-events-per-time';
import {MatCard, MatCardActions} from '@angular/material/card';
import {
  MatExpansionPanel,
  MatExpansionPanelDescription,
  MatExpansionPanelHeader,
  MatExpansionPanelTitle
} from '@angular/material/expansion';
import {MatButton} from '@angular/material/button';
import {MatList, MatListItem, MatListItemLine, MatListItemTitle} from '@angular/material/list';
import {ColorIndicatorComponent} from '../../../shared/components/color-indicator/color-indicator.component';
import {
  MedicationTypeIconComponent
} from '../../../medication/components/medication-type-icon/medication-type-icon.component';
import {FormatPipeModule, ParseIsoPipeModule} from 'ngx-date-fns';
import {MatIcon} from '@angular/material/icon';

@Component({
  selector: 'mediminder-intake-event-list',
  standalone: true,
  imports: [
    MatCard,
    MatExpansionPanel,
    MatCardActions,
    MatButton,
    MatExpansionPanelTitle,
    MatExpansionPanelDescription,
    MatExpansionPanelHeader,
    MatList,
    MatListItem,
    MatListItemTitle,
    MatListItemLine,
    ColorIndicatorComponent,
    MedicationTypeIconComponent,
    FormatPipeModule,
    ParseIsoPipeModule,
    MatIcon
  ],
  templateUrl: './intake-event-list.component.html',
  styleUrl: './intake-event-list.component.scss'
})
export class IntakeEventListComponent implements OnChanges {
  @Input({required: true})
  events!: IntakeEvent[];
  @Output()
  complete: EventEmitter<IntakeEvent> = new EventEmitter<IntakeEvent>();
  @Output()
  delete: EventEmitter<IntakeEvent> = new EventEmitter<IntakeEvent>();
  eventsPerTime: IntakeEventsPerTime[] = [];

  ngOnChanges() {
    this.eventsPerTime = groupPerTime(this.events);
  }
}
