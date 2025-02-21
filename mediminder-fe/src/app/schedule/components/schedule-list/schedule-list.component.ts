import {Component, EventEmitter, Input, Output} from '@angular/core';
import {MatExpansionModule} from '@angular/material/expansion';
import {
  FormatDistanceToNowPurePipeModule,
  FormatPipeModule,
  ParseIsoPipeModule,
  ParsePurePipeModule
} from 'ngx-date-fns';
import {MatListModule} from '@angular/material/list';
import {MatButtonModule} from '@angular/material/button';
import {RouterLink} from '@angular/router';
import {
  MedicationTypeIconComponent
} from '../../../medication/components/medication-type-icon/medication-type-icon.component';
import {ColorIndicatorComponent} from '../../../shared/components/color-indicator/color-indicator.component';
import {Schedule} from '../../models/schedule';
import {IntervalPipe} from '../../pipes/interval.pipe';

@Component({
  selector: 'mediminder-schedule-list',
  imports: [
    MatExpansionModule,
    MatListModule,
    MatButtonModule,
    ParseIsoPipeModule,
    FormatPipeModule,
    RouterLink,
    MedicationTypeIconComponent,
    ColorIndicatorComponent,
    FormatDistanceToNowPurePipeModule,
    IntervalPipe,
    ParsePurePipeModule,
  ],
  templateUrl: './schedule-list.component.html',
  styleUrl: './schedule-list.component.scss'
})
export class ScheduleListComponent {
  @Input({required: true})
  schedules!: Schedule[];
  @Output()
  delete: EventEmitter<Schedule> = new EventEmitter<Schedule>();
}
