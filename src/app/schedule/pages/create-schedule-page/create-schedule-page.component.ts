import {Component, inject, Input} from '@angular/core';
import {Router} from "@angular/router";
import {ToastrService} from "ngx-toastr";
import {ConfirmationService} from "../../../shared/services/confirmation.service";
import {ConfirmationDialogData} from "../../../shared/models/confirmation-dialog-data";
import {ScheduleService} from "../../services/schedule.service";
import {CreateSchedule} from "../../models/create-schedule";
import {Observable} from "rxjs";
import {Schedule} from "../../models/schedule";
import {AsyncPipe} from '@angular/common';
import {ScheduleFormComponent} from '../../components/schedule-form/schedule-form.component';
import {ContainerComponent} from '../../../shared/components/container/container.component';
import {HeroDescriptionDirective} from '../../../shared/components/hero/hero-description.directive';
import {HeroTitleDirective} from '../../../shared/components/hero/hero-title.directive';
import {HeroComponent} from '../../../shared/components/hero/hero.component';

@Component({
  selector: 'mediminder-create-schedule-page',
  templateUrl: './create-schedule-page.component.html',
  styleUrls: ['./create-schedule-page.component.scss'],
  standalone: true,
  imports: [
    HeroComponent,
    HeroTitleDirective,
    HeroDescriptionDirective,
    ContainerComponent,
    ScheduleFormComponent,
    AsyncPipe
  ]
})
export class CreateSchedulePageComponent {
  schedule$!: Observable<Schedule>;
  private service = inject(ScheduleService);
  private router = inject(Router);
  private toastrService = inject(ToastrService);
  private confirmationService = inject(ConfirmationService);

  @Input()
  set id(id: string) {
    this.schedule$ = this.service.findById(id);
  }

  onCancel() {
    const data: ConfirmationDialogData = {
      title: 'Are you sure you want to leave this window?',
      content: 'If you leave this window, the previously entered data will be lost.',
      okLabel: 'YES',
      cancelLabel: 'NO'
    }
    this.confirmationService
      .show(data)
      .subscribe(() => this.router.navigate(['schedule']));
  }

  onSubmit(input: CreateSchedule) {
    this.service
      .create(input)
      .subscribe({
        next: (result) => {
          this.toastrService.success(`${result.medication.name} was successfully added to your schedule`, `Medication added to schedule`);
          this.router.navigate(['schedule']);
        },
        error: (err) => {
          console.error(err);
          this.toastrService.error(`The schedule entry could not be added to your schedule`, `Error`)
        }
      });
  }
}
