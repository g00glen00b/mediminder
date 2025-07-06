import {Component, computed, model, output} from '@angular/core';
import {MedicationType} from '../../models/medication-type';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatButton} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';
import {MatStep, MatStepLabel, MatStepper, MatStepperIcon, MatStepperNext} from '@angular/material/stepper';
import {
  MedicationWizardBasicStepComponent
} from '../medication-wizard-basic-step/medication-wizard-basic-step.component';
import {MedicationWizardDoseStepComponent} from '../medication-wizard-dose-step/medication-wizard-dose-step.component';
import {AdministrationType} from '../../models/administration-type';
import {DoseType} from '../../models/dose-type';
import {
  MedicationWizardColorStepComponent
} from '../medication-wizard-color-step/medication-wizard-color-step.component';
import {Color} from '../../../shared/models/color';
import {CreateMedicationRequest} from '../../models/create-medication-request';

@Component({
  selector: 'mediminder-medication-wizard',
  imports: [
    FormsModule,
    MatButton,
    MatIcon,
    MatStep,
    MatStepLabel,
    MatStepper,
    MatStepperIcon,
    MatStepperNext,
    ReactiveFormsModule,
    MedicationWizardBasicStepComponent,
    MedicationWizardDoseStepComponent,
    MedicationWizardColorStepComponent,
  ],
  templateUrl: './medication-wizard.component.html',
  styleUrl: './medication-wizard.component.scss'
})
export class MedicationWizardComponent {
  name = model('');
  medicationType = model<MedicationType>();
  administrationType = model<AdministrationType>();
  doseType = model<DoseType>();
  dosesPerPackage = model(0);
  color = model<Color>('BLACK');
  request = computed<CreateMedicationRequest>(() => ({
    name: this.name(),
    medicationTypeId: this.medicationType()?.id || '',
    administrationTypeId: this.administrationType()?.id || '',
    doseTypeId: this.doseType()?.id || '',
    dosesPerPackage: this.dosesPerPackage(),
    color: this.color(),
  }))
  create = output<CreateMedicationRequest>();
  cancel = output<void>();
}
