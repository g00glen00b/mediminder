import {Component, inject, Input, OnChanges} from '@angular/core';
import {CabinetService} from '../../services/cabinet.service';
import {AlertComponent} from '../../../shared/components/alert/alert.component';
import {ContainerComponent} from '../../../shared/components/container/container.component';
import {HeroComponent} from '../../../shared/components/hero/hero.component';
import {HeroDescriptionDirective} from '../../../shared/components/hero/hero-description.directive';
import {HeroTitleDirective} from '../../../shared/components/hero/hero-title.directive';
import {MatAnchor} from '@angular/material/button';
import {ErrorResponse} from '../../../shared/models/error-response';
import {CabinetEntry} from '../../models/cabinet-entry';
import {Router, RouterLink} from '@angular/router';
import {UpdateCabinetEntryRequest} from '../../models/update-cabinet-entry-request';
import {ToastrService} from 'ngx-toastr';
import {ConfirmationService} from '../../../shared/services/confirmation.service';
import {CabinetEntryFormComponent} from '../../components/cabinet-entry-form/cabinet-entry-form.component';
import {CreateCabinetEntryRequest} from '../../models/create-cabinet-entry-request';

@Component({
  selector: 'mediminder-edit-cabinet-entry-page',
  imports: [
    HeroComponent,
    HeroTitleDirective,
    HeroDescriptionDirective,
    ContainerComponent,
    AlertComponent,
    CabinetEntryFormComponent,
    RouterLink,
    MatAnchor
  ],
  templateUrl: './edit-cabinet-entry-page.component.html',
  standalone: true,
  styleUrl: './edit-cabinet-entry-page.component.scss'
})
export class EditCabinetEntryPageComponent implements OnChanges {
  private readonly router = inject(Router);
  private readonly toastr = inject(ToastrService);
  private readonly confirmationService = inject(ConfirmationService);
  private readonly cabinetService = inject(CabinetService);
  @Input()
  id!: string;
  entry?: CabinetEntry;
  error?: ErrorResponse;

  ngOnChanges() {
    this.cabinetService
      .findById(this.id)
      .subscribe(entry => this.entry = entry);
  }

  submit(originalRequest: CreateCabinetEntryRequest) {
    const {expiryDate, remainingDoses} = originalRequest;
    const request: UpdateCabinetEntryRequest = {expiryDate, remainingDoses};
    this.cabinetService.update(this.id, request).subscribe({
      next: entry => {
        this.toastr.success(`Successfully updated cabinet entry for '${entry.medication.name}'`);
        this.router.navigate([`/cabinet`]);
      },
      error: response => this.error = response.error,
    })
  }

  cancel() {
    this.confirmationService.show({
      cancelLabel: 'Cancel',
      content: 'Are you sure you want to cancel editing this cabinet entry?',
      title: 'Confirm',
      okLabel: 'Confirm'
    }).subscribe(() => this.router.navigate([`/cabinet`]));
  }
}
