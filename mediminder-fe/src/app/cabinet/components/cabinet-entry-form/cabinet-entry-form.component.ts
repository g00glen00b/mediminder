import {Component, computed, input, model, OnChanges, output} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {Medication} from '../../../medication/models/medication';
import {format, parseISO} from 'date-fns';
import {CabinetEntry} from '../../models/cabinet-entry';
import {MatError, MatFormField, MatHint, MatLabel, MatSuffix} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';
import {MatAnchor, MatButton} from '@angular/material/button';
import {MatDatepicker, MatDatepickerInput, MatDatepickerToggle} from '@angular/material/datepicker';
import {CreateCabinetEntryRequest} from '../../models/create-cabinet-entry-request';
import {ActionBarComponent} from '../../../shared/components/action-bar/action-bar.component';
import {PrimaryActionsDirective} from '../../../shared/components/action-bar/primary-actions.directive';
import {SecondaryActionsDirective} from '../../../shared/components/action-bar/secondary-actions.directive';
import {RouterLink} from '@angular/router';
import {MatIcon} from '@angular/material/icon';

@Component({
  selector: 'mediminder-cabinet-entry-form',
  standalone: true,
  imports: [
    MatFormField,
    MatInput,
    MatSuffix,
    MatDatepickerInput,
    MatDatepickerToggle,
    MatDatepicker,
    MatButton,
    MatLabel,
    MatHint,
    MatError,
    FormsModule,
    ActionBarComponent,
    PrimaryActionsDirective,
    SecondaryActionsDirective,
    MatAnchor,
    RouterLink,
    MatIcon,
  ],
  templateUrl: './cabinet-entry-form.component.html',
  styleUrl: './cabinet-entry-form.component.scss'
})
export class CabinetEntryFormComponent implements OnChanges {
  okLabel = input('Add');
  cabinetEntry = input<Partial<CabinetEntry>>();
  medication = input.required<Medication>();
  hideSecondaryActions = input(true);
  cancel = output<void>();
  delete = output<void>();
  confirm = output<CreateCabinetEntryRequest>();

  remainingDoses = model(0);
  expiryDate = model(new Date());
  request = computed<CreateCabinetEntryRequest | undefined>(() => {
    const medicationId = this.medication()?.id;
    const remainingDoses = this.remainingDoses();
    const expiryDate = format(this.expiryDate(), 'yyyy-MM-dd');
    return medicationId == null ? undefined : {medicationId, remainingDoses, expiryDate};
  });


  ngOnChanges() {
    this.remainingDoses.set(this.cabinetEntry()?.remainingDoses || 0);
    this.expiryDate.set(this.cabinetEntry()?.expiryDate == undefined ? new Date() : parseISO(this.cabinetEntry()!.expiryDate!));
  }
}
