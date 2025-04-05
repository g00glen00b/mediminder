import { Directive } from '@angular/core';

@Directive({
    selector: 'list-item-description',
    standalone: true
})
export class ListItemDescriptionDirective {
  // No behaviour
  // This fixes Angular's custom element detection
  // https://github.com/angular/angular/issues/11251#issuecomment-244255512
}
