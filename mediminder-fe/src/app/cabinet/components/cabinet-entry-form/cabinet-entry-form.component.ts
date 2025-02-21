import {Component, DestroyRef, EventEmitter, inject, Input, OnChanges, OnInit, Output} from '@angular/core';
import {FormBuilder, ReactiveFormsModule, Validators} from '@angular/forms';
import {MedicationService} from '../../../medication/services/medication.service';
import {getMedicationLabel, Medication} from '../../../medication/models/medication';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {emptyPage} from '../../../shared/models/page';
import {filter, map, mergeMap, startWith, throttleTime} from 'rxjs';
import {defaultPageRequest} from '../../../shared/models/page-request';
import {format, parseISO} from 'date-fns';
import {CabinetEntry} from '../../models/cabinet-entry';
import {CreateCabinetEntryRequest} from '../../models/create-cabinet-entry-request';
import {MatError, MatFormField, MatHint, MatLabel, MatSuffix} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';
import {MatAutocomplete, MatAutocompleteTrigger, MatOption} from '@angular/material/autocomplete';
import {MatAnchor, MatButton} from '@angular/material/button';
import {RouterLink} from '@angular/router';
import {MatDatepicker, MatDatepickerInput, MatDatepickerToggle} from '@angular/material/datepicker';

@Component({
  selector: 'mediminder-cabinet-entry-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatFormField,
    MatInput,
    MatAutocompleteTrigger,
    MatAnchor,
    RouterLink,
    MatAutocomplete,
    MatOption,
    MatSuffix,
    MatDatepickerInput,
    MatDatepickerToggle,
    MatDatepicker,
    MatButton,
    MatLabel,
    MatHint,
    MatError,
  ],
  templateUrl: './cabinet-entry-form.component.html',
  styleUrl: './cabinet-entry-form.component.scss'
})
export class CabinetEntryFormComponent implements OnInit, OnChanges {
  private readonly destroyRef = inject(DestroyRef);
  private readonly formBuilder = inject(FormBuilder);
  private readonly medicationService = inject(MedicationService);
  form = this.formBuilder.group({
    medication: this.formBuilder.control<Medication | null>(null, [Validators.required]),
    remainingDoses: this.formBuilder.control(0, [Validators.required, Validators.min(0)]),
    expiryDate: this.formBuilder.control(new Date(), [Validators.required]),
  });
  medications = emptyPage<Medication>();
  @Input()
  okLabel = 'Add';
  @Input()
  cabinetEntry?: CabinetEntry;
  @Input()
  disableBasicFields: boolean = true;
  @Output()
  onCancel: EventEmitter<void> = new EventEmitter<void>();
  @Output()
  onSubmit: EventEmitter<CreateCabinetEntryRequest> = new EventEmitter<CreateCabinetEntryRequest>();

  ngOnInit() {
    this.initializeMedications();
  }

  ngOnChanges() {
    this.form.enable();
    this.form.patchValue({
      medication: this.cabinetEntry?.medication,
      remainingDoses: this.cabinetEntry?.remainingDoses || 0,
      expiryDate: this.cabinetEntry?.expiryDate == undefined ? undefined : parseISO(this.cabinetEntry.expiryDate)
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
    const expiryDate = format(this.form.get('expiryDate')!.value!, 'yyyy-MM-dd');
    const remainingDoses = this.form.get('remainingDoses')!.value!;
    const medication = this.form.get('medication')!.value!;
    this.onSubmit.emit({medicationId: medication.id, expiryDate, remainingDoses});
  }

  getMedicationLabel(medication: string | Medication | null): string {
    return getMedicationLabel(medication);
  }
}
