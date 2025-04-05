import { Directive } from '@angular/core';

@Directive({
    selector: 'list-item-title',
    standalone: true
})
export class ListItemTitleDirective {
  // No behaviour
  // This fixes Angular's custom element detection
  // https://github.com/angular/angular/issues/11251#issuecomment-244255512
}
