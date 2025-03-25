import {Component, DestroyRef, inject, input} from '@angular/core';
import {AlertComponent} from '../../../shared/components/alert/alert.component';
import {ContainerComponent} from '../../../shared/components/container/container.component';
import {HeroComponent} from '../../../shared/components/hero/hero.component';
import {HeroDescriptionDirective} from '../../../shared/components/hero/hero-description.directive';
import {HeroTitleDirective} from '../../../shared/components/hero/hero-title.directive';
import {Router} from '@angular/router';
import {ToastrService} from 'ngx-toastr';
import {ConfirmationService} from '../../../shared/services/confirmation.service';
import {takeUntilDestroyed, toObservable, toSignal} from '@angular/core/rxjs-interop';
import {switchMap} from 'rxjs';
import {ErrorResponse} from '../../../shared/models/error-response';
import {DocumentService} from '../../services/document.service';
import {CreateDocumentRequestWrapper} from '../../models/create-document-request-wrapper';
import {UpdateDocumentRequest} from '../../models/update-document-request';
import {DocumentFormComponent} from '../../components/document-form/document-form.component';

@Component({
  selector: 'mediminder-edit-document-page',
  imports: [
    AlertComponent,
    ContainerComponent,
    HeroComponent,
    HeroDescriptionDirective,
    HeroTitleDirective,
    DocumentFormComponent
  ],
  templateUrl: './edit-document-page.component.html',
  styleUrl: './edit-document-page.component.scss'
})
export class EditDocumentPageComponent {
  private readonly destroyRef = inject(DestroyRef);
  private readonly router = inject(Router);
  private readonly toastr = inject(ToastrService);
  private readonly confirmationService = inject(ConfirmationService);
  private readonly documentService = inject(DocumentService);

  id = input.required<string>();

  document = toSignal(toObservable(this.id).pipe(
    takeUntilDestroyed(this.destroyRef),
    switchMap(id => this.documentService.findById(id))
  ));
  error?: ErrorResponse;

  submit(originalRequest: CreateDocumentRequestWrapper) {
    const {request: {description, expiryDate, relatedMedicationId}} = originalRequest;
    const request: UpdateDocumentRequest = {description, expiryDate, relatedMedicationId};
    this.documentService.update(this.id(), request).subscribe({
      next: document => {
        this.toastr.success(`Successfully updated document '${document.filename}'`);
        this.router.navigate([`/document`]);
      },
      error: response => this.error = response.error,
    })
  }

  cancel() {
    this.confirmationService.show({
      cancelLabel: 'Cancel',
      content: 'Are you sure you want to cancel editing this schedule?',
      title: 'Confirm',
      okLabel: 'Confirm',
      type: 'info',
    }).subscribe(() => this.router.navigate([`/schedule`]));
  }
}
