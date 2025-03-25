import {Component, DestroyRef, inject, signal} from '@angular/core';
import {ContainerComponent} from '../../../shared/components/container/container.component';
import {EmptyStateComponent} from '../../../shared/components/empty-state/empty-state.component';
import {HeroActionsDirective} from '../../../shared/components/hero/hero-actions.directive';
import {HeroComponent} from '../../../shared/components/hero/hero.component';
import {HeroDescriptionDirective} from '../../../shared/components/hero/hero-description.directive';
import {HeroTitleDirective} from '../../../shared/components/hero/hero-title.directive';
import {MatAnchor} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';
import {RouterLink} from '@angular/router';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {ConfirmationService} from '../../../shared/services/confirmation.service';
import {ToastrService} from 'ngx-toastr';
import {defaultPageRequest} from '../../../shared/models/page-request';
import {takeUntilDestroyed, toObservable, toSignal} from '@angular/core/rxjs-interop';
import {mergeMap} from 'rxjs';
import {emptyPage} from '../../../shared/models/page';
import {ConfirmationDialogData} from '../../../shared/models/confirmation-dialog-data';
import {DocumentService} from '../../services/document.service';
import {Document} from '../../models/document';
import {DocumentListComponent} from '../../components/document-list/document-list.component';

@Component({
  selector: 'mediminder-document-overview-page',
  imports: [
    ContainerComponent,
    EmptyStateComponent,
    HeroActionsDirective,
    HeroComponent,
    HeroDescriptionDirective,
    HeroTitleDirective,
    MatAnchor,
    MatIcon,
    RouterLink,
    MatPaginator,
    DocumentListComponent
  ],
  templateUrl: './document-overview-page.component.html',
  styleUrl: './document-overview-page.component.scss'
})
export class DocumentOverviewPageComponent {
  private readonly destroyRef = inject(DestroyRef);
  private readonly service = inject(DocumentService);
  private readonly confirmationService = inject(ConfirmationService);
  private readonly toastr = inject(ToastrService);
  pageRequest = signal(defaultPageRequest(['id,asc']));
  documents = toSignal(toObservable(this.pageRequest).pipe(
    takeUntilDestroyed(this.destroyRef),
    mergeMap(pageRequest => this.service.findAll(pageRequest))
  ), {initialValue: emptyPage<Document>()});

  delete(document: Document): void {
    const data: ConfirmationDialogData = {
      okLabel: 'Confirm',
      cancelLabel: 'Cancel',
      title: 'Confirm deletion',
      content: `Are you sure you want to delete document '${document.filename}'?`,
      type: 'error',
    };
    this.confirmationService.show(data)
      .pipe(mergeMap(() => this.service.delete(document.id)))
      .subscribe({
        next: () => {
          this.toastr.success(`Successfully deleted document '${document.filename}'`);
          this.pageRequest.set({...this.pageRequest()});
        },
        error: response => this.toastr.error(response.error.detail),
      });
  }

  download(document: Document): void {
    this.service.download(document);
  }

  onPageChange(event: PageEvent) {
    this.pageRequest.set({
      ...this.pageRequest(),
      page: event.pageIndex,
      size: event.pageSize,
    });
  }
}
