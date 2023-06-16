import {Component} from '@angular/core';
import {HeroTitleDirective} from "./hero-title.directive";
import {HeroDescriptionDirective} from "./hero-description.directive";
import {HeroActionsDirective} from "./hero-actions.directive";

@Component({
  selector: 'mediminder-hero',
  templateUrl: './hero.component.html',
  styleUrls: ['./hero.component.scss'],
  standalone: true,
  imports: [
    HeroTitleDirective,
    HeroDescriptionDirective,
    HeroActionsDirective,
  ]
})
export class HeroComponent {
}
