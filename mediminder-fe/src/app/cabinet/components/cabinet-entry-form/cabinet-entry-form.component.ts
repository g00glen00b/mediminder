import {Component, computed, DestroyRef, inject, input, model, OnChanges, output, signal} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {MedicationService} from '../../../medication/services/medication.service';
import {getMedicationLabel, Medication} from '../../../medication/models/medication';
import {takeUntilDestroyed, toObservable, toSignal} from '@angular/core/rxjs-interop';
import {emptyPage} from '../../../shared/models/page';
import {mergeMap, throttleTime} from 'rxjs';
import {defaultPageRequest} from '../../../shared/models/page-request';
import {format, parseISO} from 'date-fns';
import {CabinetEntry} from '../../models/cabinet-entry';
import {MatError, MatFormField, MatHint, MatLabel, MatSuffix} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';
import {MatAutocomplete, MatAutocompleteTrigger, MatOption} from '@angular/material/autocomplete';
import {MatAnchor, MatButton} from '@angular/material/button';
import {RouterLink} from '@angular/router';
import {MatDatepicker, MatDatepickerInput, MatDatepickerToggle} from '@angular/material/datepicker';
import {CreateCabinetEntryRequest} from '../../models/create-cabinet-entry-request';

@Component({
  selector: 'mediminder-cabinet-entry-form',
  standalone: true,
  imports: [
    MatFormField,
    MatInput,
    MatAutocompleteTrigger,
    MatAnchor,
    RouterLink,
    MatAutocomplete,
    MatOption,
    MatSuffix,
    MatDatepickerInput,
    MatDatepickerToggle,
    MatDatepicker,
    MatButton,
    MatLabel,
    MatHint,
    MatError,
    FormsModule,

  ],
  templateUrl: './cabinet-entry-form.component.html',
  styleUrl: './cabinet-entry-form.component.scss'
})
export class CabinetEntryFormComponent implements OnChanges {
  private readonly destroyRef = inject(DestroyRef);
  private readonly medicationService = inject(MedicationService);

  okLabel = input('Add');
  cabinetEntry = input<CabinetEntry>();
  disableBasicFields = input(true);
  cancel = output<void>();
  confirm = output<CreateCabinetEntryRequest>();

  medication = model<Medication>();
  medicationInputValue = signal('');
  remainingDoses = model(0);
  expiryDate = model(new Date());
  medications = toSignal(toObservable(this.medicationInputValue).pipe(
    takeUntilDestroyed(this.destroyRef),
    throttleTime(300),
    mergeMap(search => this.medicationService.findAll(search || '', defaultPageRequest()))
  ), {initialValue: emptyPage<Medication>()});
  request = computed<CreateCabinetEntryRequest | undefined>(() => {
    const medicationId = this.medication()?.id;
    const remainingDoses = this.remainingDoses();
    const expiryDate = format(this.expiryDate(), 'yyyy-MM-dd');
    return medicationId == null ? undefined : {medicationId, remainingDoses, expiryDate};
  });


  ngOnChanges() {
    this.medication.set(this.cabinetEntry()?.medication);
    this.remainingDoses.set(this.cabinetEntry()?.remainingDoses || 0);
    this.expiryDate.set(this.cabinetEntry()?.expiryDate == undefined ? new Date() : parseISO(this.cabinetEntry()!.expiryDate));
  }

  getMedicationLabel(medication?: string | Medication): string {
    return getMedicationLabel(medication);
  }
}
