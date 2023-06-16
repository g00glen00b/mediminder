import {Component, EventEmitter, Input, OnChanges, Output, SimpleChanges} from '@angular/core';
import {addDays, isToday, subDays} from "date-fns";
import {MatDatepickerModule} from '@angular/material/datepicker';
import {MatMenuModule} from '@angular/material/menu';
import {DatePipe, NgIf} from '@angular/common';
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
    NgIf,
    MatMenuModule,
    MatDatepickerModule,
    DatePipe
  ]
})
export class DatePaginatorComponent implements OnChanges {
  @Input()
  date: Date = new Date();
  @Output()
  select: EventEmitter<Date> = new EventEmitter<Date>();
  isToday: boolean = true;

  ngOnChanges(changes: SimpleChanges): void {
    this.isToday = isToday(this.date);
  }

  onPreviousClick(): void {
    this.select.emit(subDays(this.date, 1));
  }

  onNextClick(): void {
    this.select.emit(addDays(this.date, 1));
  }
}
