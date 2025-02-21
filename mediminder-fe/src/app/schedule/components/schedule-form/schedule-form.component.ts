import {Component, DestroyRef, EventEmitter, inject, Input, OnChanges, OnInit, Output} from '@angular/core';
import {FormBuilder, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {MedicationService} from '../../../medication/services/medication.service';
import {getMedicationLabel, Medication} from '../../../medication/models/medication';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {emptyPage} from '../../../shared/models/page';
import {format, formatISODuration, parseISO} from 'date-fns';
import {filter, map, mergeMap, startWith, throttleTime} from 'rxjs';
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
import {Period} from '../../models/period';
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
    ReactiveFormsModule,
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
export class ScheduleFormComponent implements OnInit, OnChanges {
  private readonly destroyRef = inject(DestroyRef);
  private readonly formBuilder = inject(FormBuilder);
  private readonly medicationService = inject(MedicationService);
  form = this.formBuilder.group({
    medication: this.formBuilder.control<Medication | null>(null, [Validators.required]),
    period: this.formBuilder.group({
      startingAt: this.formBuilder.control(new Date(), [Validators.required]),
      endingAtInclusive: this.formBuilder.control<Date | null>(new Date()),
    }),
    interval: this.formBuilder.group({
      units: this.formBuilder.control(1, [Validators.required, Validators.min(1)]),
      type: this.formBuilder.control<IntervalType>('Day(s)', [Validators.required]),
    }),
    time: this.formBuilder.control(format(new Date(), 'HH:mm'), [Validators.required]),
    description: this.formBuilder.control('', [Validators.maxLength(256)]),
    dose: this.formBuilder.control(0, [Validators.required, Validators.min(0)]),
  });
  medications = emptyPage<Medication>();
  intervalTypes = intervalTypes;
  @Input()
  okLabel = 'Add';
  @Input()
  schedule?: Schedule;
  @Input()
  disableBasicFields: boolean = true;
  @Output()
  onCancel: EventEmitter<void> = new EventEmitter<void>();
  @Output()
  onSubmit: EventEmitter<CreateScheduleRequest> = new EventEmitter<CreateScheduleRequest>();

  ngOnInit() {
    this.initializeMedications();
  }

  ngOnChanges() {
    this.form.enable();
    const interval = this.schedule?.interval ? isoDurationToInterval(this.schedule?.interval) : defaultInterval;
    const startingAt = this.calculateStartingAt();
    const endingAtInclusive= this.calculateEndingAtInclusive();
    this.form.patchValue({
      medication: this.schedule?.medication || null,
      period: {startingAt, endingAtInclusive},
      interval,
      description: this.schedule?.description,
      dose: this.schedule?.dose || 0,
      time: this.schedule?.time || format(new Date(), 'HH:mm')
    });
    if (this.disableBasicFields) {
      this.form.get('medication')!.disable();
    }
  }

  initializeMedications() {
    this.form.get('medication')!.valueChanges.pipe(
      takeUntilDestroyed(this.destroyRef),
      startWith(''),
      throttleTime(300),
      filter(medication => medication == null || typeof medication === 'string'),
      map(search => search as string | null),
      mergeMap(search => this.medicationService.findAll(search || '', defaultPageRequest())))
      .subscribe(medications => this.medications = medications);
  }

  submit(): void {
    const description = this.form.get('description')!.value || undefined;
    const medication = this.form.get('medication')!.value!;
    const dose = this.form.get('dose')!.value!;
    const interval = this.calculateISOInterval();
    const startingAt = this.calculateISOStartingAt();
    const endingAtInclusive = this.calculateISOEndingAt();
    const period: Period = {startingAt, endingAtInclusive};
    const time = this.form.get('time')!.value!;
    this.onSubmit.emit({medicationId: medication.id, description, dose, interval, period, time});
  }

  getMedicationLabel(medication: string | Medication | null): string {
    return getMedicationLabel(medication);
  }

  private calculateISOStartingAt() {
    const startingAtDate = this.form.get('period')!.get('startingAt')!.value!;
    return format(startingAtDate, 'yyyy-MM-dd');
  }

  private calculateISOEndingAt(): string | undefined {
    const endingAtDate = this.form.get('period')!.get('endingAtInclusive')!.value;
    return endingAtDate == null ? undefined : format(endingAtDate, 'yyyy-MM-dd');
  }

  private calculateISOInterval(): string {
    const units = this.form.get('interval')!.get('units')!.value!;
    const type = this.form.get('interval')!.get('type')!.value!;
    const interval: Interval = {units, type};
    const duration = intervalToIsoDuration(interval);
    return removeTimeFromISODuration(formatISODuration(duration));
  }

  private calculateStartingAt(): Date {
    if (this.schedule == undefined) return new Date();
    return parseISO(this.schedule.period.startingAt);
  }

  private calculateEndingAtInclusive(): Date | undefined {
    if (this.schedule == undefined) return new Date();
    if (this.schedule.period.endingAtInclusive == undefined) return undefined;
    return parseISO(this.schedule.period.endingAtInclusive);
  }
}
