import {Component, DestroyRef, inject, input, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {ToastrService} from 'ngx-toastr';
import {ConfirmationService} from '../../../shared/services/confirmation.service';
import {ErrorResponse} from '../../../shared/models/error-response';
import {ScheduleService} from '../../services/schedule.service';
import {UpdateScheduleRequest} from '../../models/update-schedule-request';
import {CreateScheduleRequest} from '../../models/create-schedule-request';
import {AlertComponent} from '../../../shared/components/alert/alert.component';
import {ContainerComponent} from '../../../shared/components/container/container.component';
import {HeroComponent} from '../../../shared/components/hero/hero.component';
import {HeroDescriptionDirective} from '../../../shared/components/hero/hero-description.directive';
import {HeroTitleDirective} from '../../../shared/components/hero/hero-title.directive';
import {ScheduleFormComponent} from '../../components/schedule-form/schedule-form.component';
import {takeUntilDestroyed, toObservable, toSignal} from '@angular/core/rxjs-interop';
import {switchMap} from 'rxjs';
import {NavbarService} from '../../../shared/services/navbar.service';

@Component({
  selector: 'mediminder-edit-schedule-page',
  imports: [
    AlertComponent,
    ContainerComponent,
    HeroComponent,
    HeroDescriptionDirective,
    HeroTitleDirective,
    ScheduleFormComponent
  ],
  templateUrl: './edit-schedule-page.component.html',
  styleUrl: './edit-schedule-page.component.scss'
})
export class EditSchedulePageComponent implements OnInit {
  private readonly navbarService = inject(NavbarService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly router = inject(Router);
  private readonly toastr = inject(ToastrService);
  private readonly confirmationService = inject(ConfirmationService);
  private readonly scheduleService = inject(ScheduleService);

  medicationId = input.required<string>();
  id = input.required<string>();

  schedule = toSignal(toObservable(this.id).pipe(
    takeUntilDestroyed(this.destroyRef),
    switchMap(id => this.scheduleService.findById(id))
  ));
  error?: ErrorResponse;

  ngOnInit() {
    this.navbarService.enableBackButton([`/medication`, this.medicationId()]);
    this.navbarService.setTitle('Edit Schedule');
  }

  submit(originalRequest: CreateScheduleRequest) {
    const {interval, period, time, description, dose} = originalRequest;
    const request: UpdateScheduleRequest = {interval, period, time, description, dose};
    this.scheduleService.update(this.id(), request).subscribe({
      next: schedule => {
        this.toastr.success(`Successfully updated schedule for '${schedule.medication.name}'`);
        this.router.navigate([`/medication`, schedule.medication.id]);
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
    }).subscribe(() => this.router.navigate([`/medication`, this.medicationId()]));
  }

  delete() {
    this.confirmationService.show({
      cancelLabel: 'Cancel',
      content: 'Are you sure you want to delete this schedule?',
      title: 'Confirm',
      okLabel: 'Delete',
      type: 'error',
    }).pipe(
      switchMap(() => this.scheduleService.delete(this.id()))
    ).subscribe({
      next: () => {
        this.toastr.success(`Successfully deleted schedule for ${this.schedule()!.medication.name}`);
        this.router.navigate([`/medication`, this.medicationId()]);
      },
      error: response => this.error = response.error,
    })
  }
}
