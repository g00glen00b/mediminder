import {Component, inject, Input, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {ToastrService} from 'ngx-toastr';
import {MedicationService} from '../../services/medication.service';
import {ConfirmationService} from '../../../shared/services/confirmation.service';
import {Medication} from '../../models/medication';
import {ErrorResponse} from '../../../shared/models/error-response';
import {CreateMedicationRequest} from '../../models/create-medication-request';
import {HeroComponent} from '../../../shared/components/hero/hero.component';
import {ContainerComponent} from '../../../shared/components/container/container.component';
import {AlertComponent} from '../../../shared/components/alert/alert.component';
import {MedicationFormComponent} from '../../components/medication-form/medication-form.component';
import {HeroTitleDirective} from '../../../shared/components/hero/hero-title.directive';
import {HeroDescriptionDirective} from '../../../shared/components/hero/hero-description.directive';

@Component({
  selector: 'mediminder-create-medication-page',
  standalone: true,
  imports: [
    HeroComponent,
    HeroTitleDirective,
    HeroDescriptionDirective,
    ContainerComponent,
    AlertComponent,
    MedicationFormComponent
  ],
  templateUrl: './create-medication-page.component.html',
  styleUrl: './create-medication-page.component.scss'
})
export class CreateMedicationPageComponent implements OnInit {
  private readonly router = inject(Router);
  private readonly toastr = inject(ToastrService);
  private readonly medicationService = inject(MedicationService);
  private readonly confirmationService = inject(ConfirmationService);
  error?: ErrorResponse;
  @Input()
  id?: string;
  originalMedication?: Medication;

  ngOnInit() {
    this.initializeDuplicateValues();
  }

  private initializeDuplicateValues() {
    if (this.id == undefined) this.originalMedication = undefined;
    else {
      this.medicationService
        .findById(this.id)
        .subscribe(medication => this.originalMedication = medication);
    }
  }

  cancel(): void {
    this.confirmationService.show({
      cancelLabel: 'Cancel',
      content: 'Are you sure you want to cancel creating this medication?',
      title: 'Confirm',
      okLabel: 'Confirm'
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
