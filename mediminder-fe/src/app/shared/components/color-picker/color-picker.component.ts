import {Component, input, model} from '@angular/core';
import {Color, colorOptions} from '../../models/color';
import {MatRippleModule} from '@angular/material/core';

@Component({
  selector: 'mediminder-color-picker',
  imports: [
      MatRippleModule,
  ],
  templateUrl: './color-picker.component.html',
  styleUrl: './color-picker.component.scss',
  standalone: true,
})
export class ColorPickerComponent {
  colors= colorOptions();
  value = model.required<Color>();
  disabled = input<boolean>(false);
}
