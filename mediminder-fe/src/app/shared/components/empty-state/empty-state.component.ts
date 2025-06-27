import {Component, input} from '@angular/core';
import {MatIconModule} from '@angular/material/icon';

@Component({
  selector: 'mediminder-empty-state',
  templateUrl: './empty-state.component.html',
  styleUrls: ['./empty-state.component.scss'],
  standalone: true,
  imports: [
    MatIconModule
  ]
})
export class EmptyStateComponent {
  icon = input('');
  title = input('');
  spin = input(false);
}
