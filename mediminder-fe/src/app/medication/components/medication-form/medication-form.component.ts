import {Component, computed, DestroyRef, inject, input, model, OnChanges, output} from '@angular/core';
import {ColorPickerComponent} from '../../../shared/components/color-picker/color-picker.component';
import {MatButton} from '@angular/material/button';
import {MatError, MatFormField, MatHint, MatLabel} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';
import {MatOption, MatSelect} from '@angular/material/select';
import {FormsModule} from '@angular/forms';
import {MedicationTypeService} from '../../services/medication-type.service';
import {Color} from '../../../shared/models/color';
import {takeUntilDestroyed, toObservable, toSignal} from '@angular/core/rxjs-interop';
import {emptyPage} from '../../../shared/models/page';
import {MedicationType} from '../../models/medication-type';
import {DoseType} from '../../models/dose-type';
import {AdministrationType} from '../../models/administration-type';
import {Medication} from '../../models/medication';
import {filter, map, switchMap} from 'rxjs';
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
    FormsModule
  ],
  templateUrl: './medication-form.component.html',
  styleUrl: './medication-form.component.scss'
})
export class MedicationFormComponent implements OnChanges {
  private readonly destroyRef = inject(DestroyRef);
  private readonly medicationTypeService = inject(MedicationTypeService);

  okLabel = input('Add');
  medication = input<Medication>();
  disableBasicFields = input(true);
  cancel = output<void>();
  confirm = output<CreateMedicationRequest>();

  name = model('');
  medicationType = model<string | null>(null);
  administrationType = model<string | null>(null);
  dosesPerPackage = model(0);
  doseType = model<string | null>(null);
  color = model<Color>('BLACK');
  medicationTypes = toSignal(this.medicationTypeService.findAll({sort: ['name,asc']}), {initialValue: emptyPage<MedicationType>()});
  doseTypes = toSignal(toObservable(this.medicationType).pipe(
    takeUntilDestroyed(this.destroyRef),
    filter(medicationTypeId => medicationTypeId != null),
    switchMap(medicationTypeId => this.medicationTypeService.findAllDoseTypes(medicationTypeId!, defaultPageRequest())),
  ), {initialValue: emptyPage<DoseType>()});
  administrationTypes = toSignal(toObservable(this.medicationType).pipe(
    takeUntilDestroyed(this.destroyRef),
    filter(medicationTypeId => medicationTypeId != null),
    switchMap(medicationTypeId => this.medicationTypeService.findAllAdministrationTypes(medicationTypeId!, defaultPageRequest())),
  ), {initialValue: emptyPage<AdministrationType>()});
  request = computed<CreateMedicationRequest | undefined>(() => {
    const name = this.name();
    const medicationTypeId = this.medicationType();
    const administrationTypeId = this.administrationType();
    const doseTypeId = this.doseType();
    const dosesPerPackage = this.dosesPerPackage();
    const color = this.color();
    if (medicationTypeId == null || administrationTypeId == null || doseTypeId == null) return undefined;
    return {name, administrationTypeId, color, dosesPerPackage, doseTypeId, medicationTypeId};
  })


  constructor() {
    toObservable(this.administrationTypes)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        map(types => types.content.map(({id}) => id)),
        filter(ids => this.administrationType() == null || !ids.includes(this.administrationType()!)),
        map(ids => ids[0]))
      .subscribe(id => this.administrationType.set(id));
    toObservable(this.doseTypes)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        map(types => types.content.map(({id}) => id)),
        filter(ids => this.doseType() == null || !ids.includes(this.doseType()!)),
        map(ids => ids[0]))
      .subscribe(id => this.doseType.set(id));
  }

  ngOnChanges() {
    this.name.set(this.medication()?.name || '');
    this.medicationType.set(this.medication()?.medicationType?.id || null);
    this.administrationType.set(this.medication()?.administrationType?.id || null);
    this.doseType.set(this.medication()?.doseType?.id || null);
    this.dosesPerPackage.set(this.medication()?.dosesPerPackage || 0);
    this.color.set(this.medication()?.color || 'BLACK');
  }
}
