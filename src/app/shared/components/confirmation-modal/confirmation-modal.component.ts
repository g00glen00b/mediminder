import {Component, inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from "@angular/material/dialog";
import {ConfirmationDialogData} from "../../models/confirmation-dialog-data";
import {MatButtonModule} from '@angular/material/button';
import {
  CabinetEntryDialogComponent
} from "../../../cabinet/components/cabinet-entry-dialog/cabinet-entry-dialog.component";

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
  public dialogRef = inject(MatDialogRef<CabinetEntryDialogComponent>);
  public data: ConfirmationDialogData = inject(MAT_DIALOG_DATA);
}
