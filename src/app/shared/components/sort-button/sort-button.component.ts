import {Component, EventEmitter, Input, Output} from '@angular/core';
import {SortOption} from "../../models/sort-option";

@Component({
  selector: 'mediminder-sort-button',
  templateUrl: './sort-button.component.html',
  styleUrls: ['./sort-button.component.scss']
})
export class SortButtonComponent {
  @Input()
  options: SortOption[] = [];
  @Input()
  value: SortOption | undefined;
  @Output()
  change: EventEmitter<SortOption> = new EventEmitter<SortOption>();
}
