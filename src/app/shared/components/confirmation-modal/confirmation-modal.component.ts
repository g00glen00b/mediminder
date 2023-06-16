import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from "@angular/material/dialog";
import {ConfirmationDialogData} from "../../models/confirmation-dialog-data";
import {MatButtonModule} from '@angular/material/button';

@Component({
  selector: 'mediminder-confirmation-modal',
  templateUrl: './confirmation-modal.component.html',
  styleUrls: ['./confirmation-modal.component.scss'],
  standalone: true,
  imports: [
    MatDialogModule,
    MatButtonModule
  ]
})
export class ConfirmationModalComponent {
  constructor(
    private dialogRef: MatDialogRef<ConfirmationModalComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ConfirmationDialogData) {
  }

  onCancelClick(): void {
    this.dialogRef.close();
  }
}
