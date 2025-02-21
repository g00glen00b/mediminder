import {Component, EventEmitter, Input, Output} from '@angular/core';
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
  @Input({required: true})
  value!: Color;
  @Input()
  disabled = false;
  @Output()
  colorChange = new EventEmitter<Color>();
}
