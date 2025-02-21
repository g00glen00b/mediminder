import {Component, DestroyRef, inject, signal} from '@angular/core';
import {ContainerComponent} from '../../../shared/components/container/container.component';
import {EmptyStateComponent} from '../../../shared/components/empty-state/empty-state.component';
import {HeroActionsDirective} from '../../../shared/components/hero/hero-actions.directive';
import {HeroComponent} from '../../../shared/components/hero/hero.component';
import {HeroDescriptionDirective} from '../../../shared/components/hero/hero-description.directive';
import {HeroTitleDirective} from '../../../shared/components/hero/hero-title.directive';
import {MatAnchor} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {RouterLink} from '@angular/router';
import {ConfirmationService} from '../../../shared/services/confirmation.service';
import {ToastrService} from 'ngx-toastr';
import {defaultPageRequest} from '../../../shared/models/page-request';
import {takeUntilDestroyed, toObservable, toSignal} from '@angular/core/rxjs-interop';
import {mergeMap} from 'rxjs';
import {emptyPage} from '../../../shared/models/page';
import {ConfirmationDialogData} from '../../../shared/models/confirmation-dialog-data';
import {ScheduleService} from '../../services/schedule.service';
import {Schedule} from '../../models/schedule';
import {ScheduleListComponent} from '../../components/schedule-list/schedule-list.component';

@Component({
  selector: 'mediminder-schedule-overview-page',
  imports: [
    ContainerComponent,
    EmptyStateComponent,
    HeroActionsDirective,
    HeroComponent,
    HeroDescriptionDirective,
    HeroTitleDirective,
    MatAnchor,
    MatIcon,
    MatPaginator,
    RouterLink,
    ScheduleListComponent
  ],
  templateUrl: './schedule-overview-page.component.html',
  styleUrl: './schedule-overview-page.component.scss'
})
export class ScheduleOverviewPageComponent {
  private readonly destroyRef = inject(DestroyRef);
  private readonly service = inject(ScheduleService);
  private readonly confirmationService = inject(ConfirmationService);
  private readonly toastr = inject(ToastrService);
  pageRequest = signal(defaultPageRequest(['period.startingAt,asc']));
  schedules = toSignal(toObservable(this.pageRequest).pipe(
    takeUntilDestroyed(this.destroyRef),
    mergeMap(pageRequest => this.service.findAll(pageRequest))
  ), {initialValue: emptyPage<Schedule>()});

  delete(schedule: Schedule): void {
    const data: ConfirmationDialogData = {
      okLabel: 'Confirm',
      cancelLabel: 'Cancel',
      title: 'Confirm deletion',
      content: `Are you sure you want to delete the schedule for  ${schedule.medication.name}?`,
    };
    this.confirmationService.show(data)
      .pipe(mergeMap(() => this.service.delete(schedule.id)))
      .subscribe({
        next: () => {
          this.toastr.success(`Successfully deleted the schedule for ${schedule.medication.name}`);
          this.pageRequest.set({...this.pageRequest()});
        },
        error: response => this.toastr.error(response.error.detail),
      });
  }

  onPageChange($event: PageEvent) {
    this.pageRequest.set({
      ...this.pageRequest(),
      page: $event.pageIndex,
      size: $event.pageSize,
    });
  }
}
