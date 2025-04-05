import { Directive } from '@angular/core';

@Directive({
    selector: 'list-item-icon',
    standalone: true
})
export class ListItemIconDirective {
  // No behaviour
  // This fixes Angular's custom element detection
  // https://github.com/angular/angular/issues/11251#issuecomment-244255512
}
