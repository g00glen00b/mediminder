import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {map, mergeMap, Observable} from "rxjs";
import {CabinetService} from "../../services/cabinet.service";
import {CabinetEntry} from "../../models/cabinet-entry";
import {ConfirmationDialogData} from "../../../shared/models/confirmation-dialog-data";
import {CreateCabinetEntry} from "../../models/create-cabinet-entry";
import {ConfirmationService} from "../../../shared/services/confirmation.service";
import {ToastrService} from "ngx-toastr";
import {EditCabinetEntry} from "../../models/edit-cabinet-entry";

@Component({
  selector: 'mediminder-edit-cabinet-entry-page',
  templateUrl: './edit-cabinet-entry-page.component.html',
  styleUrls: ['./edit-cabinet-entry-page.component.scss']
})
export class EditCabinetEntryPageComponent implements OnInit {
  entry: CabinetEntry | null = null;

  constructor(
    private activatedRoute: ActivatedRoute,
    private service: CabinetService,
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
      .subscribe(() => this.router.navigate(['cabinet']));
  }

  onSubmit(input: CreateCabinetEntry) {
    if (this.entry != null) {
      const {units, initialUnits, expiryDate} = input;
      const request: EditCabinetEntry = {units, initialUnits, expiryDate};
      this.service
        .edit(this.entry.id, request)
        .subscribe({
          next: () => {
            this.toastrService.success(`${input.medicationName} was successfully updated`, `Medication updated to cabinet`);
            this.router.navigate(['cabinet']);
          },
          error: () => {
            this.toastrService.error(`${input.medicationName} could not be updated`, `Error`)
          }
        });
    }
  }
}
