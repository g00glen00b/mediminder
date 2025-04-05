import {Component, input} from '@angular/core';

@Component({
  selector: 'mediminder-action-header',
  imports: [],
  templateUrl: './action-header.component.html',
  styleUrl: './action-header.component.scss'
})
export class ActionHeaderComponent {
  title = input.required<string>();
}
