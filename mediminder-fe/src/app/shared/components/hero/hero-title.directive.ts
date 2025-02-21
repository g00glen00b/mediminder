import { Directive } from '@angular/core';

@Directive({
    selector: 'hero-title',
    standalone: true
})
export class HeroTitleDirective {
  // No behaviour
  // This fixes Angular's custom element detection
  // https://github.com/angular/angular/issues/11251#issuecomment-244255512
}
