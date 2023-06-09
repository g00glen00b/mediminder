import {Component} from '@angular/core';
import {mergeMap, Observable} from "rxjs";
import {ConfirmationService} from "../../../shared/services/confirmation.service";
import {ToastrService} from "ngx-toastr";
import {Router} from "@angular/router";
import {ConfirmationDialogData} from "../../../shared/models/confirmation-dialog-data";
import {Medication} from "../../models/medication";
import {MedicationService} from "../../services/medication.service";

@Component({
  selector: 'mediminder-medication-overview-page',
  templateUrl: './medication-overview-page.component.html',
  styleUrls: ['./medication-overview-page.component.scss']
})
export class MedicationOverviewPageComponent {
  medications$!: Observable<Medication[]>;

  constructor(
    private service: MedicationService,
    private confirmationService: ConfirmationService,
    private toastrService: ToastrService) {
  }

  ngOnInit() {
    this.initializeMedications();
  }

  private initializeMedications() {
    this.medications$ = this.service.findAll();
  }

  onDeleteEntry(medication: Medication) {
    const data: ConfirmationDialogData = {
      title: 'Are you sure you want to delete this medication?',
      content: 'If you continue, the medication and all related cabinet entries and schedules are removed.',
      okLabel: 'YES',
      cancelLabel: 'NO',
    };
    this.confirmationService
      .show(data)
      .pipe(mergeMap(() => this.service.delete(medication.id)))
      .subscribe({
        next: () => {
          this.toastrService.success(`${medication.name} was successfully deleted`);
          this.initializeMedications();
        },
        error: () => this.toastrService.error(`${medication.name} could not be removed`),
      });
  }
}
