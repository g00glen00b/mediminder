import {Component} from '@angular/core';
import {
  IntakeEventOverviewComponent
} from '../../../intake/pages/intake-event-overview/intake-event-overview.component';
import {ContainerComponent} from '../../../shared/components/container/container.component';
import {
  NotificationOverviewPageComponent
} from '../../../notification/pages/notification-overview-page/notification-overview-page.component';
import {HeroComponent} from '../../../shared/components/hero/hero.component';
import {HeroTitleDirective} from '../../../shared/components/hero/hero-title.directive';
import {HeroDescriptionDirective} from '../../../shared/components/hero/hero-description.directive';
import {MatFabAnchor, MatFabButton} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';
import {RouterLink} from '@angular/router';

@Component({
  selector: 'mediminder-home-page',
  imports: [
    IntakeEventOverviewComponent,
    ContainerComponent,
    NotificationOverviewPageComponent,
    HeroComponent,
    HeroTitleDirective,
    HeroDescriptionDirective,
    MatFabAnchor,
    MatIcon,
    RouterLink,
    MatFabButton,
  ],
  templateUrl: './home-page.component.html',
  standalone: true,
  styleUrl: './home-page.component.scss'
})
export class HomePageComponent {

}
