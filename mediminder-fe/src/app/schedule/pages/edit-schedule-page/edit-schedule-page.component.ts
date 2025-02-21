import {Component, inject, Input, OnChanges} from '@angular/core';
import {Router, RouterLink} from '@angular/router';
import {ToastrService} from 'ngx-toastr';
import {ConfirmationService} from '../../../shared/services/confirmation.service';
import {ErrorResponse} from '../../../shared/models/error-response';
import {Schedule} from '../../models/schedule';
import {ScheduleService} from '../../services/schedule.service';
import {UpdateScheduleRequest} from '../../models/update-schedule-request';
import {CreateScheduleRequest} from '../../models/create-schedule-request';
import {AlertComponent} from '../../../shared/components/alert/alert.component';
import {ContainerComponent} from '../../../shared/components/container/container.component';
import {HeroComponent} from '../../../shared/components/hero/hero.component';
import {HeroDescriptionDirective} from '../../../shared/components/hero/hero-description.directive';
import {HeroTitleDirective} from '../../../shared/components/hero/hero-title.directive';
import {MatAnchor} from '@angular/material/button';
import {ScheduleFormComponent} from '../../components/schedule-form/schedule-form.component';

@Component({
  selector: 'mediminder-edit-schedule-page',
  imports: [
    AlertComponent,
    ContainerComponent,
    HeroComponent,
    HeroDescriptionDirective,
    HeroTitleDirective,
    MatAnchor,
    RouterLink,
    ScheduleFormComponent
  ],
  templateUrl: './edit-schedule-page.component.html',
  styleUrl: './edit-schedule-page.component.scss'
})
export class EditSchedulePageComponent implements OnChanges {
  private readonly router = inject(Router);
  private readonly toastr = inject(ToastrService);
  private readonly confirmationService = inject(ConfirmationService);
  private readonly scheduleService = inject(ScheduleService);
  @Input()
  id!: string;
  schedule?: Schedule;
  error?: ErrorResponse;

  ngOnChanges() {
    this.scheduleService
      .findById(this.id)
      .subscribe(schedule => this.schedule = schedule);
  }

  submit(originalRequest: CreateScheduleRequest) {
    const {interval, period, time, description, dose} = originalRequest;
    const request: UpdateScheduleRequest = {interval, period, time, description, dose};
    this.scheduleService.update(this.id, request).subscribe({
      next: entry => {
        this.toastr.success(`Successfully updated schedule for '${entry.medication.name}'`);
        this.router.navigate([`/schedule`]);
      },
      error: response => this.error = response.error,
    })
  }

  cancel() {
    this.confirmationService.show({
      cancelLabel: 'Cancel',
      content: 'Are you sure you want to cancel editing this schedule?',
      title: 'Confirm',
      okLabel: 'Confirm'
    }).subscribe(() => this.router.navigate([`/schedule`]));
  }
}
