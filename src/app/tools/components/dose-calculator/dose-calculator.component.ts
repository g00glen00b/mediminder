import {Component, OnInit} from '@angular/core';
import {addMonths, addYears, set} from "date-fns";
import {MIDNIGHT} from "../../../shared/utils/date-fns-utils";
import {BehaviorSubject, delay, filter, finalize, from, mergeMap, Observable, take, tap, toArray} from "rxjs";
import {DoseMatch} from "../../models/dose-match";
import {DoseCalculationService} from "../../services/dose-calculation.service";
import {MatDialog} from "@angular/material/dialog";
import {MissingDoseDialogComponent} from "../missing-dose-dialog/missing-dose-dialog.component";

@Component({
  selector: 'mediminder-dose-calculator',
  templateUrl: './dose-calculator.component.html',
  styleUrls: ['./dose-calculator.component.scss']
})
export class DoseCalculatorComponent implements OnInit {
  date$$: BehaviorSubject<Date> = new BehaviorSubject<Date>(addMonths(set(new Date(), MIDNIGHT), 1));
  doseMatches$!: Observable<DoseMatch[]>;
  loading: boolean = false;

  constructor(private doseCalculationService: DoseCalculationService) {
  }

  ngOnInit(): void {
    this.doseMatches$ = this.date$$
      .pipe(
        delay(0),
        tap(() => this.loading = true),
        mergeMap(date => this.doseCalculationService.findUntil(date)),
        mergeMap(matches => from(matches)
          .pipe(
            filter(({requiredDoses, availableDoses}) => requiredDoses > availableDoses),
            toArray()
          )),
        tap(() => this.loading = false));
  }
}
