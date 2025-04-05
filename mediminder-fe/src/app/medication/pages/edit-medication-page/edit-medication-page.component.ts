import {Component, DestroyRef, inject, input, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {ToastrService} from 'ngx-toastr';
import {ConfirmationService} from '../../../shared/services/confirmation.service';
import {ErrorResponse} from '../../../shared/models/error-response';
import {MedicationService} from '../../services/medication.service';
import {UpdateMedicationRequest} from '../../models/update-medication-request';
import {HeroComponent} from '../../../shared/components/hero/hero.component';
import {ContainerComponent} from '../../../shared/components/container/container.component';
import {AlertComponent} from '../../../shared/components/alert/alert.component';
import {MedicationFormComponent} from '../../components/medication-form/medication-form.component';
import {HeroTitleDirective} from '../../../shared/components/hero/hero-title.directive';
import {HeroDescriptionDirective} from '../../../shared/components/hero/hero-description.directive';
import {takeUntilDestroyed, toObservable, toSignal} from '@angular/core/rxjs-interop';
import {switchMap} from 'rxjs';
import {NavbarService} from '../../../shared/services/navbar.service';

@Component({
  selector: 'mediminder-edit-medication-page',
  standalone: true,
  imports: [
    HeroComponent,
    HeroTitleDirective,
    HeroDescriptionDirective,
    ContainerComponent,
    AlertComponent,
    MedicationFormComponent
  ],
  templateUrl: './edit-medication-page.component.html',
  styleUrl: './edit-medication-page.component.scss'
})
export class EditMedicationPageComponent implements OnInit {
  private readonly navbarService = inject(NavbarService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly router = inject(Router);
  private readonly toastr = inject(ToastrService);
  private readonly confirmationService = inject(ConfirmationService);
  private readonly medicationService = inject(MedicationService);

  id = input.required<string>();
  medication = toSignal(toObservable(this.id).pipe(
    takeUntilDestroyed(this.destroyRef),
    switchMap(id => this.medicationService.findById(id))
  ));
  error?: ErrorResponse;

  ngOnInit() {
    this.navbarService.enableBackButton([`/medication`, this.id()]);
    this.navbarService.setTitle('Edit Medication');
  }

  submit(request: UpdateMedicationRequest) {
    this.medicationService.update(this.id(), request).subscribe({
      next: medication => {
        this.toastr.success(`Successfully updated '${medication.name}'`);
        this.router.navigate([`/medication`, medication.id]);
      },
      error: response => this.error = response.error,
    })
  }

  cancel() {
    this.confirmationService.show({
      cancelLabel: 'Cancel',
      content: 'Are you sure you want to cancel editing this medication?',
      title: 'Confirm',
      okLabel: 'Confirm',
      type: 'info',
    }).subscribe(() => this.router.navigate([`/medication`, this.id()]));
  }
}
