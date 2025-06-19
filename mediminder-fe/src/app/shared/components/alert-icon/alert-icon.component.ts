import {Component, input} from '@angular/core';
import {MatIcon} from '@angular/material/icon';

@Component({
  selector: 'mediminder-alert-icon',
  imports: [
    MatIcon
  ],
  templateUrl: './alert-icon.component.html',
  styleUrl: './alert-icon.component.scss'
})
export class AlertIconComponent {
  type = input.required<'success' | 'warning' | 'error' | 'info'>();
}
