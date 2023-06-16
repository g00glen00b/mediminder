import { Component } from '@angular/core';
import { IntakeOverviewComponent } from '../../../intake/components/intake-overview/intake-overview.component';
import { AlertListComponent } from '../../../alert/components/alert-list/alert-list.component';
import { ContainerComponent } from '../../../shared/components/container/container.component';
import { HeroDescriptionDirective } from '../../../shared/components/hero/hero-description.directive';
import { HeroTitleDirective } from '../../../shared/components/hero/hero-title.directive';
import { HeroComponent } from '../../../shared/components/hero/hero.component';

@Component({
  selector: 'mediminder-home-page',
  templateUrl: './home-page.component.html',
  styleUrls: ['./home-page.component.scss'],
  standalone: true,
  imports: [
    HeroComponent,
    HeroTitleDirective,
    HeroDescriptionDirective,
    ContainerComponent,
    AlertListComponent,
    IntakeOverviewComponent
  ]
})
export class HomePageComponent {

}
