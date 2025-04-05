import { Directive } from '@angular/core';

@Directive({
    selector: 'secondary-actions',
    standalone: true
})
export class SecondaryActionsDirective {
  // No behaviour
  // This fixes Angular's custom element detection
  // https://github.com/angular/angular/issues/11251#issuecomment-244255512
}
