import {Component, input, model} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {MedicationColorPickerComponent} from '../medication-color-picker/medication-color-picker.component';
import {MedicationType} from '../../models/medication-type';

@Component({
  selector: 'mediminder-medication-wizard-color-step',
  imports: [
    FormsModule,
    MedicationColorPickerComponent
  ],
  templateUrl: './medication-wizard-color-step.component.html',
  styleUrl: './medication-wizard-color-step.component.scss'
})
export class MedicationWizardColorStepComponent {
  type = input.required<MedicationType>();
  color = model.required();
}
