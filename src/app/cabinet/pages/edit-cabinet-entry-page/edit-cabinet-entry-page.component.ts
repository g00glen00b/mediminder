import {Component, inject, Input} from '@angular/core';
import {Router} from "@angular/router";
import {CabinetService} from "../../services/cabinet.service";
import {CabinetEntry} from "../../models/cabinet-entry";
import {ConfirmationDialogData} from "../../../shared/models/confirmation-dialog-data";
import {CreateCabinetEntry} from "../../models/create-cabinet-entry";
import {ConfirmationService} from "../../../shared/services/confirmation.service";
import {ToastrService} from "ngx-toastr";
import {EditCabinetEntry} from "../../models/edit-cabinet-entry";
import {CabinetEntryFormComponent} from '../../components/cabinet-entry-form/cabinet-entry-form.component';
import {ContainerComponent} from '../../../shared/components/container/container.component';
import {HeroDescriptionDirective} from '../../../shared/components/hero/hero-description.directive';
import {HeroTitleDirective} from '../../../shared/components/hero/hero-title.directive';
import {HeroComponent} from '../../../shared/components/hero/hero.component';

@Component({
  selector: 'mediminder-edit-cabinet-entry-page',
  templateUrl: './edit-cabinet-entry-page.component.html',
  styleUrls: ['./edit-cabinet-entry-page.component.scss'],
  standalone: true,
  imports: [
    HeroComponent,
    HeroTitleDirective,
    HeroDescriptionDirective,
    ContainerComponent,
    CabinetEntryFormComponent
  ]
})
export class EditCabinetEntryPageComponent {
  entry: CabinetEntry | null = null;
  private service = inject(CabinetService);
  private confirmationService = inject(ConfirmationService);
  private toastrService = inject(ToastrService);
  private router = inject(Router);

  @Input()
  set id(id: string) {
    this.service.findById(id).subscribe(entry => this.entry = entry);
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
