import {Component, computed, DestroyRef, inject, input, model, OnChanges, output} from '@angular/core';
import {MatButton} from '@angular/material/button';
import {MatError, MatFormField, MatHint, MatLabel} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';
import {MatOption, MatSelect} from '@angular/material/select';
import {FormsModule} from '@angular/forms';
import {MedicationTypeService} from '../../services/medication-type.service';
import {Color} from '../../../shared/models/color';
import {takeUntilDestroyed, toObservable, toSignal} from '@angular/core/rxjs-interop';
import {emptyPage} from '../../../shared/models/page';
import {DoseType} from '../../models/dose-type';
import {AdministrationType} from '../../models/administration-type';
import {Medication} from '../../models/medication';
import {filter, map, switchMap} from 'rxjs';
import {defaultPageRequest} from '../../../shared/models/page-request';
import {UpdateMedicationRequest} from '../../models/update-medication-request';
import {MedicationColorPickerComponent} from '../medication-color-picker/medication-color-picker.component';

@Component({
  selector: 'mediminder-medication-form',
  standalone: true,
  imports: [
    MatButton,
    MatError,
    MatFormField,
    MatHint,
    MatInput,
    MatLabel,
    MatOption,
    MatSelect,
    FormsModule,
    MedicationColorPickerComponent
  ],
  templateUrl: './medication-form.component.html',
  styleUrl: './medication-form.component.scss'
})
export class MedicationFormComponent implements OnChanges {
  private readonly destroyRef = inject(DestroyRef);
  private readonly medicationTypeService = inject(MedicationTypeService);

  okLabel = input('Add');
  medication = input.required<Medication>();
  cancel = output<void>();
  confirm = output<UpdateMedicationRequest>();

  name = model('');
  administrationType = model<string | null>(null);
  dosesPerPackage = model(0);
  doseType = model<string | null>(null);
  color = model<Color>('BLACK');
  doseTypes = toSignal(toObservable(this.medication).pipe(
    takeUntilDestroyed(this.destroyRef),
    map(medication => medication.medicationType?.id),
    filter(medicationTypeId => medicationTypeId != null),
    switchMap(medicationTypeId => this.medicationTypeService.findAllDoseTypes(medicationTypeId!, defaultPageRequest())),
  ), {initialValue: emptyPage<DoseType>()});
  administrationTypes = toSignal(toObservable(this.medication).pipe(
    takeUntilDestroyed(this.destroyRef),
    map(medication => medication.medicationType?.id),
    filter(medicationTypeId => medicationTypeId != null),
    switchMap(medicationTypeId => this.medicationTypeService.findAllAdministrationTypes(medicationTypeId!, defaultPageRequest())),
  ), {initialValue: emptyPage<AdministrationType>()});
  request = computed<UpdateMedicationRequest | undefined>(() => {
    const name = this.name();
    const administrationTypeId = this.administrationType();
    const doseTypeId = this.doseType();
    const dosesPerPackage = this.dosesPerPackage();
    const color = this.color();
    if (administrationTypeId == null || doseTypeId == null) return undefined;
    return {name, administrationTypeId, color, dosesPerPackage, doseTypeId};
  })

  ngOnChanges() {
    this.name.set(this.medication()?.name || '');
    this.administrationType.set(this.medication()?.administrationType?.id || null);
    this.doseType.set(this.medication()?.doseType?.id || null);
    this.dosesPerPackage.set(this.medication()?.dosesPerPackage || 0);
    this.color.set(this.medication()?.color || 'BLACK');
  }
}
