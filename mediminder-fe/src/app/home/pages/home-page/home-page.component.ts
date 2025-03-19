import {Component} from '@angular/core';
import {
  IntakeEventOverviewComponent
} from '../../../intake/pages/intake-event-overview/intake-event-overview.component';
import {HeroComponent} from '../../../shared/components/hero/hero.component';
import {HeroDescriptionDirective} from '../../../shared/components/hero/hero-description.directive';
import {HeroTitleDirective} from '../../../shared/components/hero/hero-title.directive';
import {ContainerComponent} from '../../../shared/components/container/container.component';
import {
  NotificationOverviewPageComponent
} from '../../../notification/pages/notification-overview-page/notification-overview-page.component';
import {HeroActionsDirective} from '../../../shared/components/hero/hero-actions.directive';
import {MatAnchor} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';
import {RouterLink} from '@angular/router';

@Component({
  selector: 'mediminder-home-page',
  imports: [
    IntakeEventOverviewComponent,
    HeroComponent,
    HeroDescriptionDirective,
    HeroTitleDirective,
    ContainerComponent,
    NotificationOverviewPageComponent,
    HeroActionsDirective,
    MatAnchor,
    MatIcon,
    RouterLink
  ],
  templateUrl: './home-page.component.html',
  standalone: true,
  styleUrl: './home-page.component.scss'
})
export class HomePageComponent {

}
