import {Component, DestroyRef, inject, signal} from '@angular/core';
import {HeroComponent} from '../../../shared/components/hero/hero.component';
import {MatAnchor} from '@angular/material/button';
import {RouterLink} from '@angular/router';
import {MatIcon} from '@angular/material/icon';
import {ContainerComponent} from '../../../shared/components/container/container.component';
import {HeroTitleDirective} from '../../../shared/components/hero/hero-title.directive';
import {HeroDescriptionDirective} from '../../../shared/components/hero/hero-description.directive';
import {HeroActionsDirective} from '../../../shared/components/hero/hero-actions.directive';
import {defaultPageRequest} from '../../../shared/models/page-request';
import {CabinetService} from '../../services/cabinet.service';
import {takeUntilDestroyed, toObservable, toSignal} from '@angular/core/rxjs-interop';
import {mergeMap} from 'rxjs';
import {emptyPage} from '../../../shared/models/page';
import {CabinetEntry} from '../../models/cabinet-entry';
import {EmptyStateComponent} from '../../../shared/components/empty-state/empty-state.component';
import {CabinetEntryListComponent} from '../../components/cabinet-entry-list/cabinet-entry-list.component';
import {ConfirmationService} from '../../../shared/services/confirmation.service';
import {ConfirmationDialogData} from '../../../shared/models/confirmation-dialog-data';
import {ToastrService} from 'ngx-toastr';
import {MatPaginator, PageEvent} from '@angular/material/paginator';

@Component({
  selector: 'mediminder-cabinet-overview-page',
  imports: [
    HeroComponent,
    HeroTitleDirective,
    HeroDescriptionDirective,
    HeroActionsDirective,
    MatAnchor,
    RouterLink,
    MatIcon,
    ContainerComponent,
    EmptyStateComponent,
    CabinetEntryListComponent,
    MatPaginator,
  ],
  templateUrl: './cabinet-overview-page.component.html',
  styleUrl: './cabinet-overview-page.component.scss'
})
export class CabinetOverviewPageComponent {
  private readonly destroyRef = inject(DestroyRef);
  private readonly service = inject(CabinetService);
  private readonly confirmationService = inject(ConfirmationService);
  private readonly toastr = inject(ToastrService);
  pageRequest = signal(defaultPageRequest(['medicationId,asc']));
  entries = toSignal(toObservable(this.pageRequest).pipe(
    takeUntilDestroyed(this.destroyRef),
    mergeMap(pageRequest => this.service.findAll(pageRequest))
  ), {initialValue: emptyPage<CabinetEntry>()});

  delete(entry: CabinetEntry): void {
    const data: ConfirmationDialogData = {
      okLabel: 'Confirm',
      cancelLabel: 'Cancel',
      title: 'Confirm deletion',
      content: `Are you sure you want to delete the cabinet entry for ${entry.medication.name}?`,
      type: 'error',
    };
    this.confirmationService.show(data)
      .pipe(mergeMap(() => this.service.delete(entry.id)))
      .subscribe({
        next: () => {
          this.toastr.success(`Successfully deleted cabinet entry for ${entry.medication.name}`);
          this.pageRequest.set({...this.pageRequest()});
        },
        error: response => this.toastr.error(response.error.detail),
      });
  }

  onPageChange(event: PageEvent) {
    this.pageRequest.set({
      ...this.pageRequest(),
      page: event.pageIndex,
      size: event.pageSize,
    });
  }
}
