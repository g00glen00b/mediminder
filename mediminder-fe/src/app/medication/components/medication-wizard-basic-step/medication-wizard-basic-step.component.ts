import {Component, inject, model} from '@angular/core';
import {MatError, MatFormField, MatLabel} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';
import {MedicationTypePickerComponent} from '../medication-type-picker/medication-type-picker.component';
import {ControlContainer, FormsModule, NgForm, ReactiveFormsModule} from '@angular/forms';
import {MedicationType} from '../../models/medication-type';
import {toSignal} from '@angular/core/rxjs-interop';
import {emptyPage} from '../../../shared/models/page';
import { MedicationTypeService } from '../../services/medication-type.service';

@Component({
  selector: 'mediminder-medication-wizard-basic-step',
  imports: [
    MatError,
    MatFormField,
    MatInput,
    MatLabel,
    MedicationTypePickerComponent,
    ReactiveFormsModule,
    FormsModule
  ],
  templateUrl: './medication-wizard-basic-step.component.html',
  styleUrl: './medication-wizard-basic-step.component.scss',
  viewProviders: [ { provide: ControlContainer, useExisting: NgForm } ]
})
export class MedicationWizardBasicStepComponent {
  private readonly medicationTypeService = inject(MedicationTypeService);

  name = model('');
  medicationType = model<MedicationType>();
  medicationTypes = toSignal(
    this.medicationTypeService.findAll({size: 50, sort: ['name,asc']}),
    {initialValue: emptyPage<MedicationType>()}
  );
}
