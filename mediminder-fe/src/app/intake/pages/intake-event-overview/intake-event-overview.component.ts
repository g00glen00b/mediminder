import {Component, DestroyRef, inject, model, signal} from '@angular/core';
import {takeUntilDestroyed, toObservable, toSignal} from '@angular/core/rxjs-interop';
import {BehaviorSubject, combineLatest, mergeMap, tap} from 'rxjs';
import {DatePaginatorComponent} from '../../../shared/components/date-paginator/date-paginator.component';
import {IntakeEventListComponent} from '../../components/intake-event-list/intake-event-list.component';
import {SwipeGestureDirective} from '../../../shared/directives/swipe-gesture.directive';
import {addDays, subDays} from 'date-fns';
import {IntakeEvent} from '../../models/intake-event';
import {ToastrService} from 'ngx-toastr';
import {EmptyStateComponent} from '../../../shared/components/empty-state/empty-state.component';
import {IntakeEventCacheService} from '../../services/intake-event-cache.service';

@Component({
  selector: 'mediminder-intake-event-overview',
  imports: [
    DatePaginatorComponent,
    IntakeEventListComponent,
    SwipeGestureDirective,
    EmptyStateComponent
  ],
  templateUrl: './intake-event-overview.component.html',
  standalone: true,
  styleUrl: './intake-event-overview.component.scss'
})
export class IntakeEventOverviewComponent {
  private readonly destroyRef = inject(DestroyRef);
  private readonly toastr = inject(ToastrService);
  private readonly service = inject(IntakeEventCacheService);
  private refresh = new BehaviorSubject<void>(undefined);
  loading = signal(true);
  targetDate = model(new Date());
  events = toSignal(combineLatest([
    toObservable(this.targetDate),
    this.refresh,
  ]).pipe(
    takeUntilDestroyed(this.destroyRef),
    tap(() => this.loading.set(true)),
    mergeMap(([targetDate]) => this.service.findAll(targetDate)),
    tap(() => this.loading.set(false)),
  ), {initialValue: []});

  onSwipeLeft() {
    this.targetDate.set(addDays(this.targetDate(), 1));
  }

  onSwipeRight() {
    this.targetDate.set(subDays(this.targetDate(), 1));
  }

  complete(event: IntakeEvent) {
    this.service.complete(event.scheduleId, this.targetDate()).subscribe({
      next: () => this.refresh.next(),
      error: () => this.toastr.error(`Could not complete ${event.medication.name}`),
    });
  }
}
