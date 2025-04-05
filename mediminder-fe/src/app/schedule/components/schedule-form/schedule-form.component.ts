import {Component, computed, input, model, OnChanges, output} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {Medication} from '../../../medication/models/medication';
import {format, formatISODuration, parseISO} from 'date-fns';
import {MatOption} from '@angular/material/autocomplete';
import {
  defaultInterval,
  Interval,
  intervalToIsoDuration,
  IntervalType,
  intervalTypes,
  isoDurationToInterval
} from '../../models/interval';
import {Schedule} from '../../models/schedule';
import {removeTimeFromISODuration} from '../../../shared/utils/date-fns-utils';
import {MatButton} from '@angular/material/button';
import {
  MatDatepickerToggle,
  MatDateRangeInput,
  MatDateRangePicker,
  MatEndDate,
  MatStartDate
} from '@angular/material/datepicker';
import {MatError, MatFormField, MatHint, MatLabel, MatSuffix} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';
import {MatSelect} from '@angular/material/select';
import {CreateScheduleRequest} from '../../models/create-schedule-request';
import {MatIcon} from '@angular/material/icon';

@Component({
  selector: 'mediminder-schedule-form',
  standalone: true,
  imports: [
    FormsModule,
    MatButton,
    MatDatepickerToggle,
    MatError,
    MatFormField,
    MatHint,
    MatInput,
    MatLabel,
    MatOption,
    MatSelect,
    MatSuffix,
    FormsModule,
    MatStartDate,
    MatDateRangeInput,
    MatEndDate,
    MatDateRangePicker,
    MatDatepickerToggle,
    MatSuffix,
    MatIcon,
  ],
  templateUrl: './schedule-form.component.html',
  styleUrl: './schedule-form.component.scss'
})
export class ScheduleFormComponent implements OnChanges {
  okLabel = input('Add');
  schedule = input<Schedule>()
  medication = input.required<Medication>();
  hideDelete = input(true);
  cancel = output<void>();
  delete = output<void>();
  confirm = output<CreateScheduleRequest>();

  startingAt = model(new Date());
  endingAtInclusive = model<Date | undefined>(undefined);
  intervalUnits = model(1);
  intervalType = model<IntervalType>('Day(s)');
  time = model(format(new Date(), 'HH:mm'));
  description = model('');
  dose = model(0);

  intervalTypes = intervalTypes;
  isoStartingAt = computed(() => format(this.startingAt(), 'yyyy-MM-dd'));
  isoEndingAtInclusive = computed(() => {
    if (this.endingAtInclusive()) return format(this.endingAtInclusive()!, 'yyyy-MM-dd');
    else return undefined
  });
  isoInterval = computed(() => {
    const interval: Interval = {units: this.intervalUnits(), type: this.intervalType()};
    const duration = intervalToIsoDuration(interval);
    return removeTimeFromISODuration(formatISODuration(duration));
  });
  request = computed<CreateScheduleRequest>(() => ({
    description: this.description(),
    dose: this.dose(),
    interval: this.isoInterval(),
    medicationId: this.medication()!.id,
    period: {startingAt: this.isoStartingAt(), endingAtInclusive: this.isoEndingAtInclusive()},
    time: this.time(),
  }));

  ngOnChanges() {
    const interval = this.schedule()?.interval ? isoDurationToInterval(this.schedule()?.interval!) : defaultInterval;
    this.startingAt.set(this.calculateStartingAt());
    this.endingAtInclusive.set(this.calculateEndingAtInclusive());
    this.intervalUnits.set(interval.units);
    this.intervalType.set(interval.type);
    this.time.set(this.schedule()?.time || format(new Date(), 'HH:mm'));
    this.description.set(this.schedule()?.description || '');
    this.dose.set(this.schedule()?.dose || 0);
  }

  private calculateStartingAt(): Date {
    if (this.schedule() == undefined) return new Date();
    return parseISO(this.schedule()!.period.startingAt);
  }

  private calculateEndingAtInclusive(): Date | undefined {
    if (this.schedule() == undefined) return undefined;
    if (this.schedule()!.period.endingAtInclusive == undefined) return undefined;
    return parseISO(this.schedule()!.period.endingAtInclusive!);
  }
}
