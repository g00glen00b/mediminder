import {Component, DestroyRef, inject, input, OnInit} from '@angular/core';
import {AlertComponent} from '../../../shared/components/alert/alert.component';
import {ContainerComponent} from '../../../shared/components/container/container.component';
import {HeroComponent} from '../../../shared/components/hero/hero.component';
import {HeroDescriptionDirective} from '../../../shared/components/hero/hero-description.directive';
import {HeroTitleDirective} from '../../../shared/components/hero/hero-title.directive';
import {Router} from '@angular/router';
import {ToastrService} from 'ngx-toastr';
import {ConfirmationService} from '../../../shared/services/confirmation.service';
import {ErrorResponse} from '../../../shared/models/error-response';
import {takeUntilDestroyed, toObservable, toSignal} from '@angular/core/rxjs-interop';
import {filter, switchMap} from 'rxjs';
import {DocumentService} from '../../services/document.service';
import {DocumentFormComponent} from '../../components/document-form/document-form.component';
import {CreateDocumentRequestWrapper} from '../../models/create-document-request-wrapper';
import {MedicationService} from '../../../medication/services/medication.service';
import {NavbarService} from '../../../shared/services/navbar.service';

@Component({
  selector: 'mediminder-create-document-page',
  imports: [
    AlertComponent,
    ContainerComponent,
    HeroComponent,
    HeroDescriptionDirective,
    HeroTitleDirective,
    DocumentFormComponent
  ],
  templateUrl: './create-document-page.component.html',
  styleUrl: './create-document-page.component.scss'
})
export class CreateDocumentPageComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  private readonly router = inject(Router);
  private readonly toastr = inject(ToastrService);
  private readonly confirmationService = inject(ConfirmationService);
  private readonly documentService = inject(DocumentService);
  private readonly medicationService = inject(MedicationService);
  private readonly navbarService = inject(NavbarService);
  id = input<string>();
  medicationId = input.required<string>();
  medication = toSignal(toObservable(this.medicationId).pipe(
    takeUntilDestroyed(this.destroyRef),
    switchMap(id => this.medicationService.findById(id))
  ));

  error?: ErrorResponse;
  originalDocument = toSignal(toObservable(this.id).pipe(
    takeUntilDestroyed(this.destroyRef),
    filter(id => id != null),
    switchMap(id => this.documentService.findById(id))
  ));

  ngOnInit() {
    this.navbarService.setTitle('Upload document');
    this.navbarService.enableBackButton(['/medication', this.medicationId()]);
  }

  cancel(): void {
    this.confirmationService.show({
      cancelLabel: 'Cancel',
      content: 'Are you sure you want to cancel creating this document?',
      title: 'Confirm',
      okLabel: 'Confirm',
      type: 'info',
    }).subscribe(() => this.router.navigate([`/medication`, this.medicationId()]));
  }

  submit(requestWrapper: CreateDocumentRequestWrapper): void {
    this.error = undefined;
    const {request, file} = requestWrapper;
    this.documentService.create(request, file!).subscribe({
      next: document => {
        this.toastr.success(`Successfully uploaded document '${document.filename}'`);
        this.router.navigate([`/medication`, this.medicationId()]);
      },
      error: response => this.error = response.error,
    })
  }
}
