import {Component, DestroyRef, inject, input} from '@angular/core';
import {ContainerComponent} from '../../../shared/components/container/container.component';
import {HeroComponent} from '../../../shared/components/hero/hero.component';
import {HeroDescriptionDirective} from '../../../shared/components/hero/hero-description.directive';
import {HeroTitleDirective} from '../../../shared/components/hero/hero-title.directive';
import {Router} from '@angular/router';
import {ConfirmationService} from '../../../shared/services/confirmation.service';
import {CreateCabinetEntryRequest} from '../../models/create-cabinet-entry-request';
import {CabinetService} from '../../services/cabinet.service';
import {ToastrService} from 'ngx-toastr';
import {ErrorResponse} from '../../../shared/models/error-response';
import {AlertComponent} from '../../../shared/components/alert/alert.component';
import {CabinetEntryFormComponent} from '../../components/cabinet-entry-form/cabinet-entry-form.component';
import {takeUntilDestroyed, toObservable, toSignal} from '@angular/core/rxjs-interop';
import {filter, switchMap} from 'rxjs';

@Component({
  selector: 'mediminder-create-cabinet-entry-page',
  imports: [
    ContainerComponent,
    HeroComponent,
    HeroDescriptionDirective,
    HeroTitleDirective,
    AlertComponent,
    CabinetEntryFormComponent,
  ],
  templateUrl: './create-cabinet-entry-page.component.html',
  styleUrl: './create-cabinet-entry-page.component.scss'
})
export class CreateCabinetEntryPageComponent {
  private readonly destroyRef = inject(DestroyRef);
  private readonly router = inject(Router);
  private readonly toastr = inject(ToastrService);
  private readonly confirmationService = inject(ConfirmationService);
  private readonly cabinetService = inject(CabinetService);
  id = input<string>();

  error?: ErrorResponse;
  originalCabinetEntry = toSignal(toObservable(this.id).pipe(
    takeUntilDestroyed(this.destroyRef),
    filter(id => id != null),
    switchMap(id => this.cabinetService.findById(id))
  ));


  cancel(): void {
    this.confirmationService.show({
      cancelLabel: 'Cancel',
      content: 'Are you sure you want to cancel creating this cabinet entry?',
      title: 'Confirm',
      okLabel: 'Confirm'
    }).subscribe(() => this.router.navigate([`/cabinet`]));
  }

  submit(request: CreateCabinetEntryRequest): void {
    this.error = undefined;
    this.cabinetService.create(request).subscribe({
      next: entry => {
        this.toastr.success(`Successfully created cabinet entry for '${entry.medication.name}'`);
        this.router.navigate([`/cabinet`]);
      },
      error: response => this.error = response.error,
    })
  }
}
