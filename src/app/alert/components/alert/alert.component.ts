import {Component, Input} from '@angular/core';
import {Alert} from "../../models/alert";
import { MatIconModule } from '@angular/material/icon';
import { NgSwitch, NgSwitchCase } from '@angular/common';

@Component({
  selector: 'mediminder-alert',
  templateUrl: './alert.component.html',
  styleUrls: ['./alert.component.scss'],
  standalone: true,
  imports: [
    NgSwitch,
    NgSwitchCase,
    MatIconModule
  ]
})
export class AlertComponent {
  @Input()
  alert!: Alert;
}
