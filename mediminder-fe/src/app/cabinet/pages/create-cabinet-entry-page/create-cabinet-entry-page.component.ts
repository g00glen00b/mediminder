import {Component, DestroyRef, inject, input, OnInit} from '@angular/core';
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
import {MedicationService} from '../../../medication/services/medication.service';
import {NavbarService} from '../../../shared/services/navbar.service';

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
export class CreateCabinetEntryPageComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  private readonly router = inject(Router);
  private readonly toastr = inject(ToastrService);
  private readonly navbarService = inject(NavbarService);
  private readonly confirmationService = inject(ConfirmationService);
  private readonly cabinetService = inject(CabinetService);
  private readonly medicationService = inject(MedicationService);
  id = input<string>();
  medicationId = input.required<string>();
  medication = toSignal(toObservable(this.medicationId).pipe(
    takeUntilDestroyed(this.destroyRef),
    switchMap(id => this.medicationService.findById(id))
  ));

  error?: ErrorResponse;
  originalCabinetEntry = toSignal(toObservable(this.id).pipe(
    takeUntilDestroyed(this.destroyRef),
    filter(id => id != null),
    switchMap(id => this.cabinetService.findById(id))
  ));

  ngOnInit() {
    this.navbarService.setTitle('Create cabinet entry');
    this.navbarService.enableBackButton(['/medication', this.medicationId()]);
  }


  cancel(): void {
    this.confirmationService.show({
      cancelLabel: 'Cancel',
      content: 'Are you sure you want to cancel creating this cabinet entry?',
      title: 'Confirm',
      okLabel: 'Confirm',
      type: 'info',
    }).subscribe(() => this.router.navigate([`/medication`, this.medicationId()]));
  }

  submit(request: CreateCabinetEntryRequest): void {
    this.error = undefined;
    this.cabinetService.create(request).subscribe({
      next: entry => {
        this.toastr.success(`Successfully created cabinet entry for '${entry.medication.name}'`);
        this.router.navigate([`/medication`, this.medicationId()]);
      },
      error: response => this.error = response.error,
    })
  }
}
