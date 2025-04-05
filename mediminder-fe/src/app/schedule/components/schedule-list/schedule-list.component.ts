import {Component, input, output} from '@angular/core';
import {MatExpansionModule} from '@angular/material/expansion';
import {
  FormatDistanceToNowPurePipeModule,
  FormatPipeModule,
  ParseIsoPipeModule,
  ParsePurePipeModule
} from 'ngx-date-fns';
import {MatListModule} from '@angular/material/list';
import {MatButtonModule} from '@angular/material/button';
import {Schedule} from '../../models/schedule';
import {MatCard, MatCardContent} from '@angular/material/card';
import {ScheduleListItemComponent} from '../schedule-list-item/schedule-list-item.component';

@Component({
  selector: 'mediminder-schedule-list',
  imports: [
    MatExpansionModule,
    MatListModule,
    MatButtonModule,
    ParseIsoPipeModule,
    FormatPipeModule,
    FormatDistanceToNowPurePipeModule,
    ParsePurePipeModule,
    MatCard,
    MatCardContent,
    ScheduleListItemComponent,
  ],
  templateUrl: './schedule-list.component.html',
  styleUrl: './schedule-list.component.scss'
})
export class ScheduleListComponent {
  schedules = input.required<Schedule[]>();
  delete = output<Schedule>();
}
