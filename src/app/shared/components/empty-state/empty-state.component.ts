import {Component, Input} from '@angular/core';

@Component({
  selector: 'mediminder-empty-state',
  templateUrl: './empty-state.component.html',
  styleUrls: ['./empty-state.component.scss']
})
export class EmptyStateComponent {
  @Input()
  icon: string = '';
  @Input()
  title: string = '';
}
