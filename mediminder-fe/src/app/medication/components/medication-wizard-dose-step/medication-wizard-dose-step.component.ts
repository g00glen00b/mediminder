import {Component, DestroyRef, inject, input, model} from '@angular/core';
import {MatError, MatFormField, MatHint, MatLabel} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';
import {MatOption} from '@angular/material/autocomplete';
import {MatSelect} from '@angular/material/select';
import {ControlContainer, FormsModule, NgForm, ReactiveFormsModule} from '@angular/forms';
import {DoseType} from '../../models/dose-type';
import {takeUntilDestroyed, toObservable, toSignal} from '@angular/core/rxjs-interop';
import {filter, map, switchMap} from 'rxjs';
import {defaultPageRequest} from '../../../shared/models/page-request';
import {emptyPage} from '../../../shared/models/page';
import {AdministrationType} from '../../models/administration-type';
import {MedicationType} from '../../models/medication-type';
import {MedicationTypeService} from '../../services/medication-type.service';

@Component({
  selector: 'mediminder-medication-wizard-dose-step',
  imports: [
    MatError,
    MatFormField,
    MatHint,
    MatInput,
    MatLabel,
    MatOption,
    MatSelect,
    ReactiveFormsModule,
    FormsModule
  ],
  templateUrl: './medication-wizard-dose-step.component.html',
  styleUrl: './medication-wizard-dose-step.component.scss',
  viewProviders: [ { provide: ControlContainer, useExisting: NgForm } ]
})
export class MedicationWizardDoseStepComponent {
  private readonly destroyRef = inject(DestroyRef);
  private readonly medicationTypeService = inject(MedicationTypeService);

  medicationType = input.required<MedicationType>();
  dosesPerPackage = model(0);
  doseType = model<DoseType>();
  doseTypes = toSignal(toObservable(this.medicationType).pipe(
    takeUntilDestroyed(this.destroyRef),
    filter(medicationTypeId => medicationTypeId != null),
    map(medicationType => medicationType.id),
    switchMap(medicationTypeId => this.medicationTypeService.findAllDoseTypes(medicationTypeId!, defaultPageRequest())),
  ), {initialValue: emptyPage<DoseType>()});
  administrationType = model<AdministrationType>();
  administrationTypes = toSignal(toObservable(this.medicationType).pipe(
    takeUntilDestroyed(this.destroyRef),
    filter(medicationTypeId => medicationTypeId != null),
    map(medicationType => medicationType.id),
    switchMap(medicationTypeId => this.medicationTypeService.findAllAdministrationTypes(medicationTypeId!, defaultPageRequest())),
  ), {initialValue: emptyPage<AdministrationType>()});

  constructor() {
    toObservable(this.administrationTypes)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        map(types => types.content),
        filter(types => this.administrationType() == null || !types.includes(this.administrationType()!)),
        map(types => types[0]))
      .subscribe(type => this.administrationType.set(type));
    toObservable(this.doseTypes)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        map(types => types.content),
        filter(types => this.doseType() == null || !types.includes(this.doseType()!)),
        map(types => types[0]))
      .subscribe(type => this.doseType.set(type));
  }
}
