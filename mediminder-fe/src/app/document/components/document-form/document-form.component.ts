import {Component, computed, input, model, OnChanges, output} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatAnchor, MatButton} from '@angular/material/button';
import {MatDatepicker, MatDatepickerInput, MatDatepickerToggle} from '@angular/material/datepicker';
import {MatError, MatFormField, MatHint, MatLabel, MatSuffix} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';
import {Medication} from '../../../medication/models/medication';
import {format, parseISO} from 'date-fns';
import {Document} from '../../models/document';
import {FileInput, MaterialFileInputModule} from 'ngx-custom-material-file-input';
import {CreateDocumentRequestWrapper} from '../../models/create-document-request-wrapper';
import {MatIcon} from '@angular/material/icon';
import {ActionBarComponent} from '../../../shared/components/action-bar/action-bar.component';
import {RouterLink} from '@angular/router';
import {PrimaryActionsDirective} from '../../../shared/components/action-bar/primary-actions.directive';
import {SecondaryActionsDirective} from '../../../shared/components/action-bar/secondary-actions.directive';

@Component({
  selector: 'mediminder-document-form',
  imports: [
    FormsModule,
    MatButton,
    MatDatepickerToggle,
    MatFormField,
    MatHint,
    MatInput,
    MatLabel,
    MatSuffix,
    ReactiveFormsModule,
    MatDatepicker,
    MatDatepickerInput,
    MaterialFileInputModule,
    MatError,
    MatIcon,
    ActionBarComponent,
    MatAnchor,
    RouterLink,
    PrimaryActionsDirective,
    SecondaryActionsDirective,
  ],
  templateUrl: './document-form.component.html',
  styleUrl: './document-form.component.scss'
})
export class DocumentFormComponent implements OnChanges {
  okLabel = input('Upload');
  document = input<Document>()
  medication = input.required<Medication>();
  disableBasicFields = input(false);
  hideSecondaryActions = input(true);
  cancel = output<void>();
  delete = output<void>();
  confirm = output<CreateDocumentRequestWrapper>();

  expiryDate = model<Date>();
  description = model('');
  file = model<FileInput>(new FileInput([]));

  requestWrapper = computed<CreateDocumentRequestWrapper>(() => ({
    file: this.file()!.files.length > 0 ? this.file()!.files[0] : undefined,
    request: {
      description: this.description(),
      relatedMedicationId: this.medication()?.id,
      expiryDate: this.expiryDate() == null ? undefined : format(this.expiryDate()!, 'yyyy-MM-dd'),
    },
  }));

  ngOnChanges() {
    this.expiryDate.set(this.document()?.expiryDate == undefined ? undefined : parseISO(this.document()!.expiryDate!));
    this.description.set(this.document()?.description || '');
  }
}
