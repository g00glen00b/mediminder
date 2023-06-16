import {Component} from '@angular/core';
import {mergeMap, Observable} from "rxjs";
import {ConfirmationService} from "../../../shared/services/confirmation.service";
import {ToastrService} from "ngx-toastr";
import {ConfirmationDialogData} from "../../../shared/models/confirmation-dialog-data";
import {Medication} from "../../models/medication";
import {MedicationService} from "../../services/medication.service";
import {AsyncPipe} from '@angular/common';
import {MedicationListComponent} from '../../components/medication-list/medication-list.component';
import {ContainerComponent} from '../../../shared/components/container/container.component';
import {HeroDescriptionDirective} from '../../../shared/components/hero/hero-description.directive';
import {HeroTitleDirective} from '../../../shared/components/hero/hero-title.directive';
import {HeroComponent} from '../../../shared/components/hero/hero.component';

@Component({
  selector: 'mediminder-medication-overview-page',
  templateUrl: './medication-overview-page.component.html',
  styleUrls: ['./medication-overview-page.component.scss'],
  standalone: true,
  imports: [
    HeroComponent,
    HeroTitleDirective,
    HeroDescriptionDirective,
    ContainerComponent,
    MedicationListComponent,
    AsyncPipe
  ]
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
