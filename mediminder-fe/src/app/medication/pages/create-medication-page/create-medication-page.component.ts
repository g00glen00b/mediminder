import {Component, inject, input} from '@angular/core';
import {Router} from '@angular/router';
import {ToastrService} from 'ngx-toastr';
import {MedicationService} from '../../services/medication.service';
import {ConfirmationService} from '../../../shared/services/confirmation.service';
import {ErrorResponse} from '../../../shared/models/error-response';
import {CreateMedicationRequest} from '../../models/create-medication-request';
import {HeroComponent} from '../../../shared/components/hero/hero.component';
import {ContainerComponent} from '../../../shared/components/container/container.component';
import {AlertComponent} from '../../../shared/components/alert/alert.component';
import {HeroTitleDirective} from '../../../shared/components/hero/hero-title.directive';
import {HeroDescriptionDirective} from '../../../shared/components/hero/hero-description.directive';
import {FormsModule} from '@angular/forms';
import {MedicationWizardComponent} from '../../components/medication-wizard/medication-wizard.component';

@Component({
  selector: 'mediminder-create-medication-page',
  standalone: true,
  imports: [
    HeroComponent,
    HeroTitleDirective,
    HeroDescriptionDirective,
    ContainerComponent,
    AlertComponent,
    FormsModule,
    MedicationWizardComponent
  ],
  templateUrl: './create-medication-page.component.html',
  styleUrl: './create-medication-page.component.scss'
})
export class CreateMedicationPageComponent {
  private readonly router = inject(Router);
  private readonly toastr = inject(ToastrService);
  private readonly medicationService = inject(MedicationService);
  private readonly confirmationService = inject(ConfirmationService);

  id = input<string>();
  error?: ErrorResponse;

  cancel(): void {
    this.confirmationService.show({
      cancelLabel: 'Cancel',
      content: 'Are you sure you want to cancel creating this medication?',
      title: 'Confirm',
      okLabel: 'Confirm',
      type: 'info',
    }).subscribe(() => this.router.navigate([`/medication`]));
  }

  submit(request: CreateMedicationRequest): void {
    this.medicationService.create(request).subscribe({
      next: medication => {
        this.toastr.success(`Successfully created '${medication.name}'`);
        this.router.navigate([`/medication`]);
      },
      error: response => this.error = response.error,
    })
  }
}
