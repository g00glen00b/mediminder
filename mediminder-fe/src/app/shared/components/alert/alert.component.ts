import {Component, input, output} from '@angular/core';
import {MatIconModule} from '@angular/material/icon';
import {MatIconButton} from '@angular/material/button';
import {AlertIconComponent} from '../alert-icon/alert-icon.component';

@Component({
  selector: 'mediminder-alert',
  templateUrl: './alert.component.html',
  styleUrls: ['./alert.component.scss'],
  standalone: true,
  imports: [
    MatIconModule,
    MatIconButton,
    AlertIconComponent,
  ]
})
export class AlertComponent {
  type = input.required<'success' | 'warning' | 'error' | 'info'>();
  closeable = input(false);
  title = input<string>();
  close = output<void>();
}
