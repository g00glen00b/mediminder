import {Component, EventEmitter, Input, OnChanges, Output, SimpleChanges} from '@angular/core';
import {addDays, isToday, subDays} from "date-fns";

@Component({
  selector: 'mediminder-date-paginator',
  templateUrl: './date-paginator.component.html',
  styleUrls: ['./date-paginator.component.scss']
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
