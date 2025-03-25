import {Component, computed, DestroyRef, inject, input, model, OnChanges, output, signal} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatAnchor, MatButton} from '@angular/material/button';
import {MatAutocomplete, MatAutocompleteTrigger, MatOption} from '@angular/material/autocomplete';
import {MatDatepicker, MatDatepickerInput, MatDatepickerToggle} from '@angular/material/datepicker';
import {MatError, MatFormField, MatHint, MatLabel, MatSuffix} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';
import {RouterLink} from '@angular/router';
import {MedicationService} from '../../../medication/services/medication.service';
import {getMedicationLabel, Medication} from '../../../medication/models/medication';
import {format, parseISO} from 'date-fns';
import {takeUntilDestroyed, toObservable, toSignal} from '@angular/core/rxjs-interop';
import {mergeMap, throttleTime} from 'rxjs';
import {defaultPageRequest} from '../../../shared/models/page-request';
import {emptyPage} from '../../../shared/models/page';
import {Document} from '../../models/document';
import {FileInput, MaterialFileInputModule} from 'ngx-custom-material-file-input';
import {CreateDocumentRequestWrapper} from '../../models/create-document-request-wrapper';

@Component({
  selector: 'mediminder-document-form',
  imports: [
    FormsModule,
    MatAnchor,
    MatAutocomplete,
    MatAutocompleteTrigger,
    MatButton,
    MatDatepickerToggle,
    MatFormField,
    MatHint,
    MatInput,
    MatLabel,
    MatOption,
    MatSuffix,
    ReactiveFormsModule,
    RouterLink,
    MatDatepicker,
    MatDatepickerInput,
    MaterialFileInputModule,
    MatError,
  ],
  templateUrl: './document-form.component.html',
  styleUrl: './document-form.component.scss'
})
export class DocumentFormComponent implements OnChanges {
  private readonly destroyRef = inject(DestroyRef);
  private readonly medicationService = inject(MedicationService);

  okLabel = input('Upload');
  document = input<Document>()
  disableBasicFields = input(true);
  cancel = output<void>();
  confirm = output<CreateDocumentRequestWrapper>();

  relatedMedication = model<Medication>();
  relatedMedicationInputValue = signal('');
  expiryDate = model<Date>();
  description = model('');
  file = model<FileInput>(new FileInput([]));

  relatedMedications = toSignal(toObservable(this.relatedMedicationInputValue).pipe(
    takeUntilDestroyed(this.destroyRef),
    throttleTime(300),
    mergeMap(search => this.medicationService.findAll(search || '', defaultPageRequest()))
  ), {initialValue: emptyPage<Medication>()});
  requestWrapper = computed<CreateDocumentRequestWrapper>(() => ({
    file: this.file()!.files.length > 0 ? this.file()!.files[0] : undefined,
    request: {
      description: this.description(),
      relatedMedicationId: this.relatedMedication()?.id,
      expiryDate: this.expiryDate() == null ? undefined : format(this.expiryDate()!, 'yyyy-MM-dd'),
    },
  }));

  ngOnChanges() {
    this.relatedMedication.set(this.document()?.relatedMedication);
    this.expiryDate.set(this.document()?.expiryDate == undefined ? undefined : parseISO(this.document()!.expiryDate!));
    this.description.set(this.document()?.description || '');
  }

  getMedicationLabel(medication?: string | Medication): string {
    return getMedicationLabel(medication);
  }
}
