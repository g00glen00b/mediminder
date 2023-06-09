import {Component, Input} from '@angular/core';
import {Alert} from "../../models/alert";

@Component({
  selector: 'mediminder-alert',
  templateUrl: './alert.component.html',
  styleUrls: ['./alert.component.scss']
})
export class AlertComponent {
  @Input()
  alert!: Alert;
}
