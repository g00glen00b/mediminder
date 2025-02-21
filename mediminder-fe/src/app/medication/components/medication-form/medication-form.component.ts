import {Component, DestroyRef, EventEmitter, inject, Input, OnChanges, OnInit, Output} from '@angular/core';
import {ColorPickerComponent} from '../../../shared/components/color-picker/color-picker.component';
import {MatButton} from '@angular/material/button';
import {MatError, MatFormField, MatHint, MatLabel} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';
import {MatOption} from '@angular/material/autocomplete';
import {MatSelect} from '@angular/material/select';
import {FormBuilder, ReactiveFormsModule, Validators} from '@angular/forms';
import {MedicationTypeService} from '../../services/medication-type.service';
import {Color} from '../../../shared/models/color';
import {takeUntilDestroyed, toSignal} from '@angular/core/rxjs-interop';
import {emptyPage} from '../../../shared/models/page';
import {MedicationType} from '../../models/medication-type';
import {DoseType} from '../../models/dose-type';
import {AdministrationType} from '../../models/administration-type';
import {Medication} from '../../models/medication';
import {filter, mergeMap} from 'rxjs';
import {defaultPageRequest} from '../../../shared/models/page-request';
import {CreateMedicationRequest} from '../../models/create-medication-request';

@Component({
  selector: 'mediminder-medication-form',
  standalone: true,
  imports: [
    ColorPickerComponent,
    MatButton,
    MatError,
    MatFormField,
    MatHint,
    MatInput,
    MatLabel,
    MatOption,
    MatSelect,
    ReactiveFormsModule
  ],
  templateUrl: './medication-form.component.html',
  styleUrl: './medication-form.component.scss'
})
export class MedicationFormComponent implements OnInit, OnChanges {
  private readonly destroyRef = inject(DestroyRef);
  private readonly formBuilder = inject(FormBuilder);
  private readonly medicationTypeService = inject(MedicationTypeService);
  form = this.formBuilder.group({
    name: this.formBuilder.control<string>('', [Validators.required]),
    medicationType: this.formBuilder.control<string | null>(null, [Validators.required]),
    administrationType: this.formBuilder.control<string | null>(null, [Validators.required]),
    dosesPerPackage: this.formBuilder.control(0, [Validators.required, Validators.min(0)]),
    doseType: this.formBuilder.control<string | null>(null, [Validators.required]),
    color: this.formBuilder.control<Color>('BLACK', [Validators.required]),
  });
  medicationTypes = toSignal(this.medicationTypeService.findAll({sort: ['name,asc']}), {initialValue: emptyPage<MedicationType>()});
  doseTypes = emptyPage<DoseType>();
  administrationTypes = emptyPage<AdministrationType>();
  @Input()
  okLabel = 'Add';
  @Input()
  medication?: Medication;
  @Input()
  disableBasicFields: boolean = true;
  @Output()
  onCancel: EventEmitter<void> = new EventEmitter<void>();
  @Output()
  onSubmit: EventEmitter<CreateMedicationRequest> = new EventEmitter<CreateMedicationRequest>();

  ngOnInit() {
    this.initializeDoseTypes();
    this.initializeAdministrationTypes();
  }

  ngOnChanges() {
    this.form.patchValue({
      name: this.medication?.name || '',
      medicationType: this.medication?.medicationType?.id || null,
      administrationType: this.medication?.administrationType?.id || null,
      doseType: this.medication?.doseType?.id || null,
      dosesPerPackage: this.medication?.dosesPerPackage || 0,
      color: this.medication?.color || 'BLACK'
    });
    if (this.disableBasicFields) {
      this.form.get('medicationType')!.disable();
      this.form.get('doseType')!.disable();
    } else {
      if (this.medicationTypes().totalElements > 1) this.form.get('medicationType')!.enable();
      if (this.doseTypes.totalElements > 1) this.form.get('doseType')!.enable();
    }
  }

  private initializeDoseTypes() {
    const doseTypeControl = this.form.get('doseType')!;
    doseTypeControl.disable();
    this.form.get('medicationType')!.valueChanges.pipe(
      takeUntilDestroyed(this.destroyRef),
      filter(medicationTypeId => medicationTypeId != null),
      mergeMap(medicationTypeId => this.medicationTypeService.findAllDoseTypes(medicationTypeId!, defaultPageRequest())))
      .subscribe(types => {
        this.doseTypes = types;
        doseTypeControl.enable();
        const doseTypeIds = this.doseTypes.content.map(({id}) => id);
        if (doseTypeControl.value == null || !doseTypeIds.includes(doseTypeControl.value!)) {
          doseTypeControl.setValue(this.doseTypes.content[0].id);
        }
        if (this.doseTypes.content.length <= 1) doseTypeControl.disable();
      });
  }

  private initializeAdministrationTypes() {
    var administrationTypeControl = this.form.get('administrationType')!;
    administrationTypeControl.disable();
    this.form.get('medicationType')!.valueChanges.pipe(
      takeUntilDestroyed(this.destroyRef),
      filter(medicationTypeId => medicationTypeId != null),
      mergeMap(medicationTypeId => this.medicationTypeService.findAllAdministrationTypes(medicationTypeId!, defaultPageRequest())))
      .subscribe(types => {
        this.administrationTypes = types;
        administrationTypeControl.enable();
        const administrationTypeIds = this.administrationTypes.content.map(({id}) => id);
        if (administrationTypeControl.value == null || !administrationTypeIds.includes(administrationTypeControl.value!)) {
          administrationTypeControl.setValue(this.administrationTypes.content[0].id);
        }
        if (this.administrationTypes.content.length <= 1) administrationTypeControl.disable();
      });
  }

  submit(): void {
    const name = this.form.get('name')!.value!;
    const medicationTypeId = this.form.get('medicationType')!.value!;
    const administrationTypeId = this.form.get('administrationType')!.value!;
    const doseTypeId = this.form.get('doseType')!.value!;
    const dosesPerPackage = this.form.get('dosesPerPackage')!.value!;
    const color = this.form.get('color')!.value!;
    const request: CreateMedicationRequest = {name, administrationTypeId, color, dosesPerPackage, doseTypeId, medicationTypeId};
    this.onSubmit.emit(request);
  }
}
