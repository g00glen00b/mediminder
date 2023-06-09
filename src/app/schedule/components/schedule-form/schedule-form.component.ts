import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {CabinetEntry} from "../../../cabinet/models/cabinet-entry";
import {CreateCabinetEntry} from "../../../cabinet/models/create-cabinet-entry";
import {FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";
import {combineLatest, map, Observable, startWith} from "rxjs";
import {Medication} from "../../../medication/models/medication";
import {MedicationType} from "../../../medication/models/medication-type";
import {MedicationService} from "../../../medication/services/medication.service";
import {MedicationTypeService} from "../../../medication/services/medication-type.service";
import {Schedule} from "../../models/schedule";
import {CreateSchedule} from "../../models/create-schedule";
import {RECURRENCE_TYPES, ScheduleRecurrenceTypeWrapper} from "../../models/schedule-recurrence-type-wrapper";
import {format} from "date-fns";
import {ScheduleRecurrenceType} from "../../models/schedule-recurrence-type";
import {SchedulePeriod} from "../../models/schedule-period";
import {ScheduleRecurrence} from "../../models/schedule-recurrence";
import {ToastrService} from "ngx-toastr";

@Component({
  selector: 'mediminder-schedule-form',
  templateUrl: './schedule-form.component.html',
  styleUrls: ['./schedule-form.component.scss']
})
export class ScheduleFormComponent implements OnInit, OnChanges {
  @Input()
  schedule: Schedule | null = null;
  @Input()
  submitLabel: string = 'CREATE';
  @Input()
  scheduleReadonly: boolean = false;
  @Output()
  submit: EventEmitter<CreateSchedule> = new EventEmitter<CreateSchedule>();
  @Output()
  cancel: EventEmitter<void> = new EventEmitter<void>();
  form!: FormGroup;
  medication$!: Observable<Medication[]>;
  unitType: string = '';
  recurrenceTypes: ScheduleRecurrenceTypeWrapper[] = [...RECURRENCE_TYPES];

  constructor(
    private formBuilder: FormBuilder,
    private medicationService: MedicationService,
    private toastrService: ToastrService) {
  }

  ngOnInit(): void {
    this.initializeFormGroup();
    this.initializeMedicationObservable();
    this.initializeUnitType();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.form != null) {
      if (this.scheduleReadonly) {
        this.form.get('medication')!.disable();
      } else {
        this.form.get('medication')!.enable();
      }
      if (this.schedule != null) {
        this.form.get('medication')!.setValue(this.schedule.medication);
        this.form.get('dose')!.setValue(this.schedule.dose);
        this.form.get('startingAt')!.setValue(this.schedule.period.startingAt);
        this.form.get('endingAtInclusive')!.setValue(this.schedule.period.endingAtInclusive);
        this.form.get('recurrenceUnits')!.setValue(this.schedule.recurrence.units);
        this.form.get('recurrenceType')!.setValue(this.recurrenceTypes.find(({type}) => type === this.schedule!.recurrence.type));
        this.form.get('time')!.setValue(this.schedule.time);
        this.form.get('description')!.setValue(this.schedule.description);
      } else {
        this.form.get('medication')!.setValue(null);
        this.form.get('dose')!.setValue(1);
        this.form.get('startingAt')!.setValue(new Date());
        this.form.get('endingAtInclusive')!.setValue(null);
        this.form.get('recurrenceUnits')!.setValue(1);
        this.form.get('recurrenceType')!.setValue(RECURRENCE_TYPES[0]);
        this.form.get('time')!.setValue(format(new Date(), 'HH:mm'));
        this.form.get('description')!.setValue('');
      }
    }
  }

  submitForm(event: Event): void {
    event.preventDefault();
    event.stopPropagation();
    if (!this.form.valid) {
      this.toastrService.error('The form is incomplete');
    } else {
      const medicationId: string = this.form.get('medication')!.value.id;
      const dose: number = this.form.get('dose')!.value;
      const startingAt: Date = this.form.get('startingAt')!.value;
      const endingAtInclusive: Date = this.form.get('endingAtInclusive')!.value;
      const recurrenceUnits: number = this.form.get('recurrenceUnits')!.value;
      const recurrenceType: ScheduleRecurrenceType = this.form.get('recurrenceType')!.value.type;
      const time: string = this.form.get('time')!.value;
      const description: string = this.form.get('description')!.value;
      const period: SchedulePeriod = {startingAt, endingAtInclusive};
      const recurrence: ScheduleRecurrence = {type: recurrenceType, units: recurrenceUnits};
      this.submit.emit({medicationId, period, time, recurrence, description, dose});
    }
  }

  compareMedication(medication1: Medication | null, medication2: Medication | null): boolean {
    return medication1 != null && medication2 != null && medication1.id === medication2.id;
  }

  compareRecurrenceType(type1: ScheduleRecurrenceTypeWrapper | null, type2: ScheduleRecurrenceTypeWrapper | null): boolean {
    return type1 != null && type2 != null && type1.type === type2.type;
  }

  private initializeMedicationObservable() {
    this.medication$ = this.medicationService.findAll();
  }

  private initializeFormGroup() {
    this.form = this.formBuilder.group({
      medication: new FormControl(null, [Validators.required]),
      dose: new FormControl(1, [Validators.required, Validators.min(0)]),
      startingAt: new FormControl(new Date(), [Validators.required]),
      endingAtInclusive: new FormControl(new Date()),
      recurrenceUnits: new FormControl(1, [Validators.required, Validators.min(1)]),
      recurrenceType: new FormControl(RECURRENCE_TYPES[0], [Validators.required]),
      time: new FormControl(format(new Date(), 'HH:mm'), [Validators.required]),
      description: new FormControl('', [Validators.maxLength(256)])
    });
  }

  private initializeUnitType() {
    this.form.get('medication')!.valueChanges.subscribe(medication => {
      if (medication == null) {
        this.unitType = '';
      } else {
        const {type: {unit}} = medication as Medication;
        this.unitType = unit;
      }
    });
  }
}
