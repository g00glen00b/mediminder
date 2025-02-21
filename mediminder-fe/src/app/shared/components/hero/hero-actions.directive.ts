import { Directive } from '@angular/core';

@Directive({
    selector: 'hero-actions',
    standalone: true
})
export class HeroActionsDirective {
  // No behaviour
  // This fixes Angular's custom element detection
  // https://github.com/angular/angular/issues/11251#issuecomment-244255512
}
