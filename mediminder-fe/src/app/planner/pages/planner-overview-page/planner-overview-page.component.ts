import {Component, DestroyRef, inject, model, signal} from '@angular/core';
import {ContainerComponent} from '../../../shared/components/container/container.component';
import {HeroComponent} from '../../../shared/components/hero/hero.component';
import {HeroDescriptionDirective} from '../../../shared/components/hero/hero-description.directive';
import {HeroTitleDirective} from '../../../shared/components/hero/hero-title.directive';
import {addMonths} from 'date-fns';
import {DatePaginatorComponent} from '../../../shared/components/date-paginator/date-paginator.component';
import {takeUntilDestroyed, toObservable, toSignal} from '@angular/core/rxjs-interop';
import {PlannerService} from '../../services/planner.service';
import {combineLatest, mergeMap} from 'rxjs';
import {defaultPageRequest} from '../../../shared/models/page-request';
import {emptyPage} from '../../../shared/models/page';
import {MedicationPlan} from '../../models/medication-plan';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {PlannerListComponent} from '../../components/planner-list/planner-list.component';

@Component({
  selector: 'mediminder-planner-overview-page',
  standalone: true,
  imports: [
    ContainerComponent,
    HeroComponent,
    HeroDescriptionDirective,
    HeroTitleDirective,
    DatePaginatorComponent,
    MatPaginator,
    PlannerListComponent,
  ],
  templateUrl: './planner-overview-page.component.html',
  styleUrl: './planner-overview-page.component.scss'
})
export class PlannerOverviewPageComponent {
  private readonly service = inject(PlannerService);
  private readonly destroyRef = inject(DestroyRef);
  date = model(addMonths(new Date(), 1));
  minDate = new Date();
  pageRequest = signal(defaultPageRequest(['name,asc']));
  plans = toSignal(combineLatest([
    toObservable(this.date),
    toObservable(this.pageRequest)
  ]).pipe(
    takeUntilDestroyed(this.destroyRef),
    mergeMap(([targetDate, pageRequest]) => this.service.findAll(targetDate, pageRequest))
  ), {initialValue: emptyPage<MedicationPlan>()});

  onPageChange(event: PageEvent) {
    this.pageRequest.set({
      ...this.pageRequest(),
      page: event.pageIndex,
      size: event.pageSize,
    });
  }
}
