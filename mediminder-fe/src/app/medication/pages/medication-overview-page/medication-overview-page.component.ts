import {Component, DestroyRef, inject, signal} from '@angular/core';
import {ContainerComponent} from '../../../shared/components/container/container.component';
import {EmptyStateComponent} from '../../../shared/components/empty-state/empty-state.component';
import {MedicationListComponent} from '../../components/medication-list/medication-list.component';
import {defaultPageRequest} from '../../../shared/models/page-request';
import {takeUntilDestroyed, toObservable, toSignal} from '@angular/core/rxjs-interop';
import {mergeMap} from 'rxjs';
import {emptyPage} from '../../../shared/models/page';
import {MedicationService} from '../../services/medication.service';
import {Medication} from '../../models/medication';
import {HeroComponent} from '../../../shared/components/hero/hero.component';
import {HeroTitleDirective} from '../../../shared/components/hero/hero-title.directive';
import {HeroDescriptionDirective} from '../../../shared/components/hero/hero-description.directive';
import {MatFabAnchor} from '@angular/material/button';
import {RouterLink} from '@angular/router';
import {MatIcon} from '@angular/material/icon';

@Component({
  selector: 'mediminder-medication-overview-page',
  imports: [
    MedicationListComponent,
    ContainerComponent,
    EmptyStateComponent,
    HeroComponent,
    HeroTitleDirective,
    HeroDescriptionDirective,
    MatFabAnchor,
    MatIcon,
    RouterLink,
  ],
  templateUrl: './medication-overview-page.component.html',
  styleUrl: './medication-overview-page.component.scss'
})
export class MedicationOverviewPageComponent {
  private readonly destroyRef = inject(DestroyRef);
  private readonly service = inject(MedicationService);
  pageRequest = signal(defaultPageRequest(['name,asc']));
  medications = toSignal(toObservable(this.pageRequest).pipe(
    takeUntilDestroyed(this.destroyRef),
    mergeMap(pageRequest => this.service.findAll('', pageRequest))
  ), {initialValue: emptyPage<Medication>()});
}
