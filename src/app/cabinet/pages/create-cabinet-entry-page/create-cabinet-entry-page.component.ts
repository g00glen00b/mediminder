import {Component, OnInit} from '@angular/core';
import {CabinetService} from "../../services/cabinet.service";
import {CreateCabinetEntry} from "../../models/create-cabinet-entry";
import {ActivatedRoute, Router} from "@angular/router";
import {Toast, ToastrService} from "ngx-toastr";
import {ConfirmationService} from "../../../shared/services/confirmation.service";
import {ConfirmationDialogData} from "../../../shared/models/confirmation-dialog-data";
import {da} from "date-fns/locale";
import {filter, map, mergeMap, Observable} from "rxjs";
import {CabinetEntry} from "../../models/cabinet-entry";

@Component({
  selector: 'mediminder-create-cabinet-entry-page',
  templateUrl: './create-cabinet-entry-page.component.html',
  styleUrls: ['./create-cabinet-entry-page.component.scss']
})
export class CreateCabinetEntryPageComponent implements OnInit {
  entry$!: Observable<CabinetEntry>;

  constructor(
    private service: CabinetService,
    private activatedRoute: ActivatedRoute,
    private router: Router,
    private toastrService: ToastrService,
    private confirmationService: ConfirmationService) {
  }

  ngOnInit(): void {
    this.entry$ = this.activatedRoute.paramMap
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
      .subscribe(() => this.router.navigate(['cabinet']));
  }

  onSubmit(input: CreateCabinetEntry) {
    this.service
      .create(input)
      .subscribe({
        next: () => {
          this.toastrService.success(`${input.medicationName} was successfully added to your cabinet`, `Medication added to cabinet`);
          this.router.navigate(['cabinet']);
        },
        error: () => {
          this.toastrService.error(`${input.medicationName} could not be added to your cabinet`, `Error`)
        }
      });
  }
}
