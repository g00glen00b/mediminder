import {Component, inject, Input} from '@angular/core';
import {Router} from "@angular/router";
import {ConfirmationService} from "../../../shared/services/confirmation.service";
import {ToastrService} from "ngx-toastr";
import {ConfirmationDialogData} from "../../../shared/models/confirmation-dialog-data";
import {ScheduleService} from "../../services/schedule.service";
import {Schedule} from "../../models/schedule";
import {CreateSchedule} from "../../models/create-schedule";
import {EditSchedule} from "../../models/edit-schedule";
import {ScheduleFormComponent} from '../../components/schedule-form/schedule-form.component';
import {ContainerComponent} from '../../../shared/components/container/container.component';
import {HeroDescriptionDirective} from '../../../shared/components/hero/hero-description.directive';
import {HeroTitleDirective} from '../../../shared/components/hero/hero-title.directive';
import {HeroComponent} from '../../../shared/components/hero/hero.component';

@Component({
  selector: 'mediminder-edit-schedule-page',
  templateUrl: './edit-schedule-page.component.html',
  styleUrls: ['./edit-schedule-page.component.scss'],
  standalone: true,
  imports: [
    HeroComponent,
    HeroTitleDirective,
    HeroDescriptionDirective,
    ContainerComponent,
    ScheduleFormComponent
  ]
})
export class EditSchedulePageComponent {
  schedule: Schedule | null = null;
  private service = inject(ScheduleService);
  private confirmationService = inject(ConfirmationService);
  private toastrService = inject(ToastrService);
  private router = inject(Router);

  @Input()
  set id(id: string) {
    this.service.findById(id).subscribe(schedule => this.schedule = schedule);
  }

  onCancel() {
    const data: ConfirmationDialogData = {
      title: 'Are you sure you want to leave this window?',
      content: 'If you leave this window, the previously entered changes will be lost.',
      okLabel: 'YES',
      cancelLabel: 'NO'
    }
    this.confirmationService
      .show(data)
      .subscribe(() => this.router.navigate(['schedule']));
  }

  onSubmit(input: CreateSchedule) {
    if (this.schedule != null) {
      const {dose, recurrence, description, time, period} = input;
      const request: EditSchedule = {dose, recurrence, description, time, period};
      this.service
        .edit(this.schedule.id, request)
        .subscribe({
          next: (result) => {
            this.toastrService.success(`Schedule for ${result.medication.name} was successfully updated`, `Medication added to cabinet`);
            this.router.navigate(['schedule']);
          },
          error: () => {
            this.toastrService.error(`The schedule could not be updated`, `Error`)
          }
        });
    }
  }
}
