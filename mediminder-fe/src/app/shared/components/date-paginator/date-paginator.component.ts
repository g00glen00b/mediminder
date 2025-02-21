import {Component, EventEmitter, Input, OnChanges, Output} from '@angular/core';
import {addDays, isSameDay, isToday, subDays} from 'date-fns';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {MatMenuModule} from '@angular/material/menu';
import {DatePipe} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';

@Component({
  selector: 'mediminder-date-paginator',
  templateUrl: './date-paginator.component.html',
  styleUrls: ['./date-paginator.component.scss'],
  standalone: true,
  imports: [
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatDatepickerModule,
    DatePipe
  ]
})
export class DatePaginatorComponent implements OnChanges {
  @Input()
  date: Date = new Date();
  @Input()
  minDate?: Date;
  @Output()
  select: EventEmitter<Date> = new EventEmitter<Date>();
  isToday: boolean = true;
  previousDisabled: boolean = false;

  ngOnChanges(): void {
    this.isToday = isToday(this.date);
    this.previousDisabled = this.minDate != null && isSameDay(this.date, this.minDate);
  }

  onPreviousClick(): void {
    this.select.emit(subDays(this.date, 1));
  }

  onNextClick(): void {
    this.select.emit(addDays(this.date, 1));
  }
}
