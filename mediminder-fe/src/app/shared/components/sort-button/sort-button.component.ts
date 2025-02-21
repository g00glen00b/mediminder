import {Component, EventEmitter, Input, Output} from '@angular/core';
import {SortOption} from "../../models/sort-option";
import {MatIconModule} from '@angular/material/icon';
import {MatMenuModule} from '@angular/material/menu';
import {MatButtonModule} from '@angular/material/button';

@Component({
  selector: 'mediminder-sort-button',
  templateUrl: './sort-button.component.html',
  styleUrls: ['./sort-button.component.scss'],
  standalone: true,
  imports: [
    MatButtonModule,
    MatMenuModule,
    MatIconModule
  ]
})
export class SortButtonComponent {
  @Input()
  options: SortOption[] = [];
  @Input()
  value: SortOption | undefined;
  @Output()
  change: EventEmitter<SortOption> = new EventEmitter<SortOption>();
}
