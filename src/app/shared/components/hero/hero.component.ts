import {Component, Input} from '@angular/core';
import {Breadcrumb} from "../../models/breadcrumb";

@Component({
  selector: 'mediminder-hero',
  templateUrl: './hero.component.html',
  styleUrls: ['./hero.component.scss']
})
export class HeroComponent {
  @Input()
  breadcrumbs: Breadcrumb[] = [];
}
