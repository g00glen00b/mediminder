import {Component, DestroyRef, inject, input} from '@angular/core';
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
import {CreateScheduleRequest} from '../../models/create-schedule-request';
import {takeUntilDestroyed, toObservable, toSignal} from '@angular/core/rxjs-interop';
import {filter, switchMap} from 'rxjs';

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
  private readonly destroyRef = inject(DestroyRef);
  private readonly router = inject(Router);
  private readonly toastr = inject(ToastrService);
  private readonly confirmationService = inject(ConfirmationService);
  private readonly scheduleService = inject(ScheduleService);
  error?: ErrorResponse;
  id = input<string>();
  originalSchedule = toSignal(toObservable(this.id).pipe(
    takeUntilDestroyed(this.destroyRef),
    filter(id => id != null),
    switchMap(id => this.scheduleService.findById(id))
  ));

  cancel(): void {
    this.confirmationService.show({
      cancelLabel: 'Cancel',
      content: 'Are you sure you want to cancel creating this schedule?',
      title: 'Confirm',
      okLabel: 'Confirm',
      type: 'info',
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
