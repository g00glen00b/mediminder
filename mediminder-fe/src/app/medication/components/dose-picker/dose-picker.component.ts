import {Component, input, output} from '@angular/core';
import {DoseType} from '../../models/dose-type';

@Component({
  selector: 'mediminder-dose-picker',
  imports: [],
  templateUrl: './dose-picker.component.html',
  styleUrl: './dose-picker.component.scss'
})
export class DosePickerComponent {
  doses = input.required<number[]>();
  doseType = input.required<DoseType>();
  select = output<number>();
}
