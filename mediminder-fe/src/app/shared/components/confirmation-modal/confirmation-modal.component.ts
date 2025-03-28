import {Component, inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from "@angular/material/dialog";
import {ConfirmationDialogData} from "../../models/confirmation-dialog-data";
import {MatButtonModule} from '@angular/material/button';
import {AlertIconComponent} from '../alert-icon/alert-icon.component';

@Component({
    selector: 'mediminder-confirmation-modal',
    templateUrl: './confirmation-modal.component.html',
    styleUrls: ['./confirmation-modal.component.scss'],
  imports: [
    MatDialogModule,
    MatButtonModule,
    AlertIconComponent
  ]
})
export class ConfirmationModalComponent {
  public readonly dialogRef = inject(MatDialogRef);
  public readonly data: ConfirmationDialogData = inject(MAT_DIALOG_DATA);
  buttonType: 'primary' | 'warn' = this.data.type === 'info' ? 'primary' : 'warn';
}
