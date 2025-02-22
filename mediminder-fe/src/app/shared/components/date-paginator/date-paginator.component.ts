import {Component, computed, input, model} from '@angular/core';
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
export class DatePaginatorComponent {
  date = model(new Date());
  minDate = input<Date>();
  isToday = computed(() => isToday(this.date()));
  previousDisabled = computed(() => this.minDate() != null && isSameDay(this.date(), this.minDate()!));

  onPreviousClick(): void {
    this.date.set(subDays(this.date(), 1));
  }

  onNextClick(): void {
    this.date.set(addDays(this.date(), 1));
  }
}
