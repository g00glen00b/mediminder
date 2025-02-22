import {Component, input} from '@angular/core';
import {Color} from '../../models/color';

@Component({
  selector: 'mediminder-color-indicator',
  standalone: true,
  imports: [],
  templateUrl: './color-indicator.component.html',
  styleUrl: './color-indicator.component.scss'
})
export class ColorIndicatorComponent {
  color = input.required<Color>();
}
