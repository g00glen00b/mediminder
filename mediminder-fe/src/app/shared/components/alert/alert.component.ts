import {Component, EventEmitter, Input, Output} from '@angular/core';
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
  @Input({required: true})
  type!: 'success' | 'warning' | 'error' | 'info';
  @Input()
  closeable: boolean = false;
  @Input()
  title?: string;
  @Output()
  onClose = new EventEmitter<void>();
}
