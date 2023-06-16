import { Directive } from '@angular/core';

@Directive({
    selector: 'hero-description',
    standalone: true
})
export class HeroDescriptionDirective {
  // No behaviour
  // This fixes Angular's custom element detection
  // https://github.com/angular/angular/issues/11251#issuecomment-244255512
}
