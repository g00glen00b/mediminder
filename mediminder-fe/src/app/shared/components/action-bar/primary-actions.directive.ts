import { Directive } from '@angular/core';

@Directive({
    selector: 'primary-actions',
    standalone: true
})
export class PrimaryActionsDirective {
  // No behaviour
  // This fixes Angular's custom element detection
  // https://github.com/angular/angular/issues/11251#issuecomment-244255512
}
