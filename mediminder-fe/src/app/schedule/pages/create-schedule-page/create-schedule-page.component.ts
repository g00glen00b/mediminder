import {Component, inject, Input} from '@angular/core';
import {AlertComponent} from '../../../shared/components/alert/alert.component';
import {ContainerComponent} from '../../../shared/components/container/container.component';
import {HeroComponent} from '../../../shared/components/hero/hero.component';
import {HeroDescriptionDirective} from '../../../shared/components/hero/hero-description.directive';
import {HeroTitleDirective} from '../../../shared/components/hero/hero-title.directive';
import {ScheduleFormComponent} from '../../components/schedule-form/schedule-form.component';
import {Router} from '@angular/router';
import {ToastrService} from 'ngx-toastr';
import {ConfirmationService} from '../../../shared/services/confirmation.service';
import {ErrorResponse} from '../../../shared/models/error-response';
import {ScheduleService} from '../../services/schedule.service';
import {Schedule} from '../../models/schedule';
import {CreateScheduleRequest} from '../../models/create-schedule-request';

@Component({
  selector: 'mediminder-create-schedule-page',
  imports: [
    AlertComponent,
    ContainerComponent,
    HeroComponent,
    HeroDescriptionDirective,
    HeroTitleDirective,
    ScheduleFormComponent
  ],
  templateUrl: './create-schedule-page.component.html',
  styleUrl: './create-schedule-page.component.scss'
})
export class CreateSchedulePageComponent {
  private readonly router = inject(Router);
  private readonly toastr = inject(ToastrService);
  private readonly confirmationService = inject(ConfirmationService);
  private readonly scheduleService = inject(ScheduleService);
  error?: ErrorResponse;
  @Input()
  id?: string;
  originalSchedule?: Schedule;

  ngOnInit() {
    this.initializeDuplicateValues();
  }

  private initializeDuplicateValues() {
    if (this.id == undefined) this.originalSchedule = undefined;
    else {
      this.scheduleService
        .findById(this.id)
        .subscribe(schedule => this.originalSchedule = schedule);
    }
  }

  cancel(): void {
    this.confirmationService.show({
      cancelLabel: 'Cancel',
      content: 'Are you sure you want to cancel creating this schedule?',
      title: 'Confirm',
      okLabel: 'Confirm'
    }).subscribe(() => this.router.navigate([`/schedule`]));
  }

  submit(request: CreateScheduleRequest): void {
    this.error = undefined;
    this.scheduleService.create(request).subscribe({
      next: schedule => {
        this.toastr.success(`Successfully created schedule for '${schedule.medication.name}'`);
        this.router.navigate([`/schedule`]);
      },
      error: response => this.error = response.error,
    })
  }
}
