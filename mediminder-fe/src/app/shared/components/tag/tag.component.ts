import {Component, input} from '@angular/core';

@Component({
  selector: 'mediminder-tag',
  imports: [],
  templateUrl: './tag.component.html',
  styleUrl: './tag.component.scss'
})
export class TagComponent {
  color = input<'default' | 'warning'>('default');
}
