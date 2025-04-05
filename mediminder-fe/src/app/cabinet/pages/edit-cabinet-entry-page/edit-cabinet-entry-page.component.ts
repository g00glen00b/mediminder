import {Component, DestroyRef, inject, input, OnInit} from '@angular/core';
import {CabinetService} from '../../services/cabinet.service';
import {AlertComponent} from '../../../shared/components/alert/alert.component';
import {ContainerComponent} from '../../../shared/components/container/container.component';
import {HeroComponent} from '../../../shared/components/hero/hero.component';
import {HeroDescriptionDirective} from '../../../shared/components/hero/hero-description.directive';
import {HeroTitleDirective} from '../../../shared/components/hero/hero-title.directive';
import {ErrorResponse} from '../../../shared/models/error-response';
import {Router} from '@angular/router';
import {UpdateCabinetEntryRequest} from '../../models/update-cabinet-entry-request';
import {ToastrService} from 'ngx-toastr';
import {ConfirmationService} from '../../../shared/services/confirmation.service';
import {CabinetEntryFormComponent} from '../../components/cabinet-entry-form/cabinet-entry-form.component';
import {CreateCabinetEntryRequest} from '../../models/create-cabinet-entry-request';
import {takeUntilDestroyed, toObservable, toSignal} from '@angular/core/rxjs-interop';
import {switchMap} from 'rxjs';
import {NavbarService} from '../../../shared/services/navbar.service';

@Component({
  selector: 'mediminder-edit-cabinet-entry-page',
  imports: [
    HeroComponent,
    HeroTitleDirective,
    HeroDescriptionDirective,
    ContainerComponent,
    AlertComponent,
    CabinetEntryFormComponent
  ],
  templateUrl: './edit-cabinet-entry-page.component.html',
  standalone: true,
  styleUrl: './edit-cabinet-entry-page.component.scss'
})
export class EditCabinetEntryPageComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  private readonly router = inject(Router);
  private readonly toastr = inject(ToastrService);
  private readonly confirmationService = inject(ConfirmationService);
  private readonly cabinetService = inject(CabinetService);
  private readonly navbarService = inject(NavbarService);

  medicationId = input.required<string>();
  id = input.required<string>();

  entry = toSignal(toObservable(this.id).pipe(
    takeUntilDestroyed(this.destroyRef),
    switchMap(id => this.cabinetService.findById(id))
  ));
  error?: ErrorResponse;

  ngOnInit() {
    this.navbarService.setTitle('Edit cabinet entry');
    this.navbarService.enableBackButton(['/medication', this.medicationId()]);
  }

  submit(originalRequest: CreateCabinetEntryRequest) {
    const {expiryDate, remainingDoses} = originalRequest;
    const request: UpdateCabinetEntryRequest = {expiryDate, remainingDoses};
    this.cabinetService.update(this.id(), request).subscribe({
      next: entry => {
        this.toastr.success(`Successfully updated cabinet entry for '${entry.medication.name}'`);
        this.router.navigate([`/medication`, this.medicationId()]);
      },
      error: response => this.error = response.error,
    })
  }

  cancel() {
    this.confirmationService.show({
      cancelLabel: 'Cancel',
      content: 'Are you sure you want to cancel editing this cabinet entry?',
      title: 'Confirm',
      okLabel: 'Confirm',
      type: 'info',
    }).subscribe(() => this.router.navigate([`/medication`, this.medicationId()]));
  }

  delete() {
    this.confirmationService.show({
      cancelLabel: 'Cancel',
      content: 'Are you sure you want to delete this cabinet entry?',
      title: 'Confirm',
      okLabel: 'Delete',
      type: 'error',
    }).pipe(
      switchMap(() => this.cabinetService.delete(this.id()))
    ).subscribe({
      next: () => {
        this.toastr.success(`Successfully deleted cabinet entry for ${this.entry()!.medication.name}`);
        this.router.navigate([`/medication`, this.medicationId()]);
      },
      error: response => this.error = response.error,
    })
  }
}
