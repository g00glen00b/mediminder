import {Component, OnInit} from '@angular/core';
import {CabinetService} from "../../../cabinet/services/cabinet.service";
import {ActivatedRoute, Router} from "@angular/router";
import {ToastrService} from "ngx-toastr";
import {ConfirmationService} from "../../../shared/services/confirmation.service";
import {ConfirmationDialogData} from "../../../shared/models/confirmation-dialog-data";
import {CreateCabinetEntry} from "../../../cabinet/models/create-cabinet-entry";
import {ScheduleService} from "../../services/schedule.service";
import {CreateSchedule} from "../../models/create-schedule";
import {filter, map, mergeMap, Observable} from "rxjs";
import {Schedule} from "../../models/schedule";

@Component({
  selector: 'mediminder-create-schedule-page',
  templateUrl: './create-schedule-page.component.html',
  styleUrls: ['./create-schedule-page.component.scss']
})
export class CreateSchedulePageComponent implements OnInit {
  schedule$!: Observable<Schedule>;

  constructor(
    private service: ScheduleService,
    private activatedRoute: ActivatedRoute,
    private router: Router,
    private toastrService: ToastrService,
    private confirmationService: ConfirmationService) {
  }

  ngOnInit(): void {
    this.schedule$ = this.activatedRoute.paramMap
      .pipe(
        map(params => params.get('id')),
        filter(id => id != null),
        mergeMap(id => this.service.findById(id as string)));
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
