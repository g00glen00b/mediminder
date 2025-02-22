import {Component, input, output} from '@angular/core';
import {MatIconModule} from '@angular/material/icon';
import {NgSwitch, NgSwitchCase} from '@angular/common';
import {MatIconButton} from '@angular/material/button';

@Component({
  selector: 'mediminder-alert',
  templateUrl: './alert.component.html',
  styleUrls: ['./alert.component.scss'],
  standalone: true,
  imports: [
    NgSwitch,
    NgSwitchCase,
    MatIconModule,
    MatIconButton,
  ]
})
export class AlertComponent {
  type = input.required<'success' | 'warning' | 'error' | 'info'>();
  closeable = input(false);
  title = input<string>();
  close = output<void>();
}
