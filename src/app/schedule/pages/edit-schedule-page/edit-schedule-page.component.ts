import { Component } from '@angular/core';
import {CabinetEntry} from "../../../cabinet/models/cabinet-entry";
import {ActivatedRoute, Router} from "@angular/router";
import {CabinetService} from "../../../cabinet/services/cabinet.service";
import {ConfirmationService} from "../../../shared/services/confirmation.service";
import {ToastrService} from "ngx-toastr";
import {map, mergeMap} from "rxjs";
import {ConfirmationDialogData} from "../../../shared/models/confirmation-dialog-data";
import {CreateCabinetEntry} from "../../../cabinet/models/create-cabinet-entry";
import {EditCabinetEntry} from "../../../cabinet/models/edit-cabinet-entry";
import {ScheduleService} from "../../services/schedule.service";
import {Schedule} from "../../models/schedule";
import {CreateSchedule} from "../../models/create-schedule";
import {EditSchedule} from "../../models/edit-schedule";
import { ScheduleFormComponent } from '../../components/schedule-form/schedule-form.component';
import { ContainerComponent } from '../../../shared/components/container/container.component';
import { HeroDescriptionDirective } from '../../../shared/components/hero/hero-description.directive';
import { HeroTitleDirective } from '../../../shared/components/hero/hero-title.directive';
import { HeroComponent } from '../../../shared/components/hero/hero.component';

@Component({
    selector: 'mediminder-edit-schedule-page',
    templateUrl: './edit-schedule-page.component.html',
    styleUrls: ['./edit-schedule-page.component.scss'],
    standalone: true,
    imports: [HeroComponent, HeroTitleDirective, HeroDescriptionDirective, ContainerComponent, ScheduleFormComponent]
})
export class EditSchedulePageComponent {
  entry: Schedule | null = null;

  constructor(
    private activatedRoute: ActivatedRoute,
    private service: ScheduleService,
    private confirmationService: ConfirmationService,
    private toastrService: ToastrService,
    private router: Router) {
  }

  ngOnInit(): void {
    this.activatedRoute.paramMap
      .pipe(
        map(params => params.get('id')!),
        mergeMap(id => this.service.findById(id)))
      .subscribe(entry => this.entry = entry);
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
    if (this.entry != null) {
      const {dose, recurrence, description, time, period} = input;
      const request: EditSchedule = {dose, recurrence, description, time, period};
      this.service
        .edit(this.entry.id, request)
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
