import {inject, Injectable} from '@angular/core';
import {MatDialog} from "@angular/material/dialog";
import {ConfirmationDialogData} from "../models/confirmation-dialog-data";
import {filter, Observable} from "rxjs";
import {ConfirmationModalComponent} from "../components/confirmation-modal/confirmation-modal.component";

@Injectable({
  providedIn: 'root'
})
export class ConfirmationService {
  private dialog = inject(MatDialog);

  show(data: ConfirmationDialogData): Observable<boolean> {
    return this.dialog
      .open(ConfirmationModalComponent, {data})
      .afterClosed()
      .pipe(filter(value => value));
  }
}
