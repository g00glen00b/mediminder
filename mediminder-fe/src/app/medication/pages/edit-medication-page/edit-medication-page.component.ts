import {Component, inject, Input, OnChanges} from '@angular/core';
import {Router} from '@angular/router';
import {ToastrService} from 'ngx-toastr';
import {ConfirmationService} from '../../../shared/services/confirmation.service';
import {ErrorResponse} from '../../../shared/models/error-response';
import {MedicationService} from '../../services/medication.service';
import {Medication} from '../../models/medication';
import {UpdateMedicationRequest} from '../../models/update-medication-request';
import {CreateMedicationRequest} from '../../models/create-medication-request';
import {HeroComponent} from '../../../shared/components/hero/hero.component';
import {ContainerComponent} from '../../../shared/components/container/container.component';
import {AlertComponent} from '../../../shared/components/alert/alert.component';
import {MedicationFormComponent} from '../../components/medication-form/medication-form.component';
import {HeroTitleDirective} from '../../../shared/components/hero/hero-title.directive';
import {HeroDescriptionDirective} from '../../../shared/components/hero/hero-description.directive';

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
export class EditMedicationPageComponent implements OnChanges {
  private readonly router = inject(Router);
  private readonly toastr = inject(ToastrService);
  private readonly confirmationService = inject(ConfirmationService);
  private readonly medicationService = inject(MedicationService);
  @Input()
  id!: string;
  medication?: Medication;
  error?: ErrorResponse;

  ngOnChanges() {
    this.medicationService
      .findById(this.id)
      .subscribe(medication => this.medication = medication);
  }

  submit(originalRequest: CreateMedicationRequest) {
    const {name, administrationTypeId, color, dosesPerPackage, doseTypeId} = originalRequest;
    const request: UpdateMedicationRequest = {name, administrationTypeId, color, dosesPerPackage, doseTypeId};
    this.medicationService.update(this.id, request).subscribe({
      next: medication => {
        this.toastr.success(`Successfully updated '${medication.name}'`);
        this.router.navigate([`/medication`]);
      },
      error: response => this.error = response.error,
    })
  }

  cancel() {
    this.confirmationService.show({
      cancelLabel: 'Cancel',
      content: 'Are you sure you want to cancel editing this medication?',
      title: 'Confirm',
      okLabel: 'Confirm'
    }).subscribe(() => this.router.navigate([`/medication`]));
  }
}
