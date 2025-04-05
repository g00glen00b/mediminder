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
import {IntervalPipe} from '../../pipes/interval.pipe';
import {MatCard, MatCardContent} from '@angular/material/card';
import {MatIcon} from '@angular/material/icon';
import {DecimalPipe} from '@angular/common';
import {RouterLink} from '@angular/router';

@Component({
  selector: 'mediminder-schedule-list',
  imports: [
    MatExpansionModule,
    MatListModule,
    MatButtonModule,
    ParseIsoPipeModule,
    FormatPipeModule,
    FormatDistanceToNowPurePipeModule,
    IntervalPipe,
    ParsePurePipeModule,
    MatCard,
    MatCardContent,
    MatIcon,
    DecimalPipe,
    RouterLink,
  ],
  templateUrl: './schedule-list.component.html',
  styleUrl: './schedule-list.component.scss'
})
export class ScheduleListComponent {
  schedules = input.required<Schedule[]>();
  delete = output<Schedule>();
}
