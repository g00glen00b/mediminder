import {Component, input, output} from '@angular/core';

@Component({
  selector: 'mediminder-dose-picker',
  imports: [],
  templateUrl: './dose-picker.component.html',
  styleUrl: './dose-picker.component.scss'
})
export class DosePickerComponent {
  doses = input.required<number[]>();
  select = output<number>();
}
