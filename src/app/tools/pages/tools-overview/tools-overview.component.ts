import { Component } from '@angular/core';
import { DoseCalculatorComponent } from '../../components/dose-calculator/dose-calculator.component';
import { ContainerComponent } from '../../../shared/components/container/container.component';
import { HeroDescriptionDirective } from '../../../shared/components/hero/hero-description.directive';
import { HeroTitleDirective } from '../../../shared/components/hero/hero-title.directive';
import { HeroComponent } from '../../../shared/components/hero/hero.component';

@Component({
  selector: 'mediminder-tools-overview',
  templateUrl: './tools-overview.component.html',
  styleUrls: ['./tools-overview.component.scss'],
  standalone: true,
  imports: [
    HeroComponent,
    HeroTitleDirective,
    HeroDescriptionDirective,
    ContainerComponent,
    DoseCalculatorComponent
  ]
})
export class ToolsOverviewComponent {

}
