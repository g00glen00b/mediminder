import {Component, inject, Input} from '@angular/core';
import {CabinetService} from "../../services/cabinet.service";
import {CreateCabinetEntry} from "../../models/create-cabinet-entry";
import {Router} from "@angular/router";
import {ToastrService} from "ngx-toastr";
import {ConfirmationService} from "../../../shared/services/confirmation.service";
import {ConfirmationDialogData} from "../../../shared/models/confirmation-dialog-data";
import {Observable} from "rxjs";
import {CabinetEntry} from "../../models/cabinet-entry";
import {AsyncPipe} from '@angular/common';
import {CabinetEntryFormComponent} from '../../components/cabinet-entry-form/cabinet-entry-form.component';
import {ContainerComponent} from '../../../shared/components/container/container.component';
import {HeroDescriptionDirective} from '../../../shared/components/hero/hero-description.directive';
import {HeroTitleDirective} from '../../../shared/components/hero/hero-title.directive';
import {HeroComponent} from '../../../shared/components/hero/hero.component';

@Component({
  selector: 'mediminder-create-cabinet-entry-page',
  templateUrl: './create-cabinet-entry-page.component.html',
  styleUrls: ['./create-cabinet-entry-page.component.scss'],
  standalone: true,
  imports: [
    HeroComponent,
    HeroTitleDirective,
    HeroDescriptionDirective,
    ContainerComponent,
    CabinetEntryFormComponent,
    AsyncPipe
  ]
})
export class CreateCabinetEntryPageComponent {
  entry$!: Observable<CabinetEntry>;
  private service = inject(CabinetService);
  private router = inject(Router);
  private toastrService = inject(ToastrService);
  private confirmationService = inject(ConfirmationService);

  @Input()
  set id(id: string) {
    this.entry$ = this.service.findById(id);
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
