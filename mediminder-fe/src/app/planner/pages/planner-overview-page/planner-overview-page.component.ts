import {Component, DestroyRef, inject, signal} from '@angular/core';
import {ContainerComponent} from '../../../shared/components/container/container.component';
import {HeroComponent} from '../../../shared/components/hero/hero.component';
import {HeroDescriptionDirective} from '../../../shared/components/hero/hero-description.directive';
import {HeroTitleDirective} from '../../../shared/components/hero/hero-title.directive';
import {DatePaginatorComponent} from '../../../shared/components/date-paginator/date-paginator.component';
import {PlannerService} from '../../services/planner.service';
import {defaultPageRequest} from '../../../shared/models/page-request';
import {emptyPage} from '../../../shared/models/page';
import {MedicationPlan} from '../../models/medication-plan';
import {PlannerListComponent} from '../../components/planner-list/planner-list.component';
import {takeUntilDestroyed, toObservable, toSignal} from '@angular/core/rxjs-interop';
import {mergeMap, tap} from 'rxjs';
import {addMonths} from 'date-fns';
import {EmptyStateComponent} from '../../../shared/components/empty-state/empty-state.component';

@Component({
  selector: 'mediminder-planner-overview-page',
  standalone: true,
  imports: [
    ContainerComponent,
    HeroComponent,
    HeroDescriptionDirective,
    HeroTitleDirective,
    DatePaginatorComponent,
    PlannerListComponent,
    EmptyStateComponent,

  ],
  templateUrl: './planner-overview-page.component.html',
  styleUrl: './planner-overview-page.component.scss'
})
export class PlannerOverviewPageComponent {
  private readonly service = inject(PlannerService);
  private readonly destroyRef = inject(DestroyRef);
  loading = signal(true);
  date = signal(addMonths(new Date(), 1));
  minDate = new Date();
  plans = toSignal(toObservable(this.date).pipe(
    takeUntilDestroyed(this.destroyRef),
    tap(() => this.loading.set(true)),
    mergeMap(targetDate => this.service.findAll(targetDate, defaultPageRequest(['name,asc']))),
    tap(() => this.loading.set(false)),
  ), {initialValue: emptyPage<MedicationPlan>()});
}
