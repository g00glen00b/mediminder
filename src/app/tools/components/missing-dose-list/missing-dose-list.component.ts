import {Component, Input} from '@angular/core';
import {DoseMatch} from "../../models/dose-match";
import {MissingDoseDialogComponent} from "../missing-dose-dialog/missing-dose-dialog.component";
import {MatDialog} from "@angular/material/dialog";

@Component({
  selector: 'mediminder-missing-dose-list',
  templateUrl: './missing-dose-list.component.html',
  styleUrls: ['./missing-dose-list.component.scss']
})
export class MissingDoseListComponent {
  @Input()
  missingDoses: DoseMatch[] = [];

  constructor(private dialog: MatDialog) {
  }

  onMatchClick(match: DoseMatch) {
    this.dialog.open(MissingDoseDialogComponent, {data: match, height: '100vh', width: '100vw', maxWidth: '100vw', maxHeight: '100vh'});
  }
}
