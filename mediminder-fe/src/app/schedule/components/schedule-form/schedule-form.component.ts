import {Component, computed, DestroyRef, inject, input, model, OnChanges, output, signal} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {MedicationService} from '../../../medication/services/medication.service';
import {getMedicationLabel, Medication} from '../../../medication/models/medication';
import {takeUntilDestroyed, toObservable, toSignal} from '@angular/core/rxjs-interop';
import {emptyPage} from '../../../shared/models/page';
import {format, formatISODuration, parseISO} from 'date-fns';
import {mergeMap, throttleTime} from 'rxjs';
import {defaultPageRequest} from '../../../shared/models/page-request';
import {MatAutocomplete, MatAutocompleteTrigger, MatOption} from '@angular/material/autocomplete';
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
import {MatAnchor, MatButton} from '@angular/material/button';
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
import {RouterLink} from '@angular/router';

@Component({
  selector: 'mediminder-schedule-form',
  standalone: true,
  imports: [
    FormsModule,
    MatAutocomplete,
    MatAutocompleteTrigger,
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
    MatAnchor,
    RouterLink,
    MatStartDate,
    MatDateRangeInput,
    MatEndDate,
    MatDateRangePicker,
    MatDatepickerToggle,
    MatSuffix,

  ],
  templateUrl: './schedule-form.component.html',
  styleUrl: './schedule-form.component.scss'
})
export class ScheduleFormComponent implements OnChanges {
  private readonly destroyRef = inject(DestroyRef);
  private readonly medicationService = inject(MedicationService);

  okLabel = input('Add');
  schedule = input<Schedule>()
  disableBasicFields = input(true);
  cancel = output<void>();
  confirm = output<CreateScheduleRequest>();

  medication = model<Medication>();
  medicationInputValue = signal('');
  startingAt = model(new Date());
  endingAtInclusive = model<Date | undefined>(new Date());
  intervalUnits = model(1);
  intervalType = model<IntervalType>('Day(s)');
  time = model(format(new Date(), 'HH:mm'));
  description = model('');
  dose = model(0);

  medications = toSignal(toObservable(this.medicationInputValue).pipe(
    takeUntilDestroyed(this.destroyRef),
    throttleTime(300),
    mergeMap(search => this.medicationService.findAll(search || '', defaultPageRequest()))
  ), {initialValue: emptyPage<Medication>()});
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
    this.medication.set(this.schedule()?.medication);
    this.startingAt.set(this.calculateStartingAt());
    this.endingAtInclusive.set(this.calculateEndingAtInclusive());
    this.intervalUnits.set(interval.units);
    this.intervalType.set(interval.type);
    this.time.set(this.schedule()?.time || format(new Date(), 'HH:mm'));
    this.description.set(this.schedule()?.description || '');
    this.dose.set(this.schedule()?.dose || 0);
  }

  getMedicationLabel(medication?: string | Medication): string {
    return getMedicationLabel(medication);
  }

  private calculateStartingAt(): Date {
    if (this.schedule() == undefined) return new Date();
    return parseISO(this.schedule()!.period.startingAt);
  }

  private calculateEndingAtInclusive(): Date | undefined {
    if (this.schedule() == undefined) return new Date();
    if (this.schedule()!.period.endingAtInclusive == undefined) return undefined;
    return parseISO(this.schedule()!.period.endingAtInclusive!);
  }
}
