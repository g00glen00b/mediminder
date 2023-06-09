import {Component, OnInit} from '@angular/core';
import {IntakeService} from '../../services/intake.service';
import {BehaviorSubject, filter, mergeMap, Observable} from 'rxjs';
import {Intake} from '../../models/intake';
import {ToastrService} from 'ngx-toastr';
import {MatDialog} from '@angular/material/dialog';
import {IntakeDialogComponent} from '../intake-dialog/intake-dialog.component';
import {Router} from "@angular/router";

@Component({
  selector: 'mediminder-intake-overview',
  templateUrl: './intake-overview.component.html',
  styleUrls: ['./intake-overview.component.scss']
})
export class IntakeOverviewComponent implements OnInit {
  date$$: BehaviorSubject<Date> = new BehaviorSubject<Date>(new Date());
  intakes$: Observable<Intake[]> = new Observable<Intake[]>();

  constructor(
    private service: IntakeService,
    private router: Router,
    private toastrService: ToastrService) {
  }

  ngOnInit(): void {
    this.initializeIntakes();
  }

  onComplete(intake: Intake): void {
    this.service.complete(intake).subscribe({
      next: (intake) => {
        this.toastrService.success(`Well done for taking ${intake.schedule.medication.name}!`);
        this.initializeIntakes();
      },
      error: () => {
        this.toastrService.error('The medication intake could not be registered due to an unknown problem');
      }
    })
  }

  onOpenSchedule(intake: Intake): void {
    this.router.navigate(['schedule', intake.schedule.id, 'edit']);
  }

  private initializeIntakes() {
    this.intakes$ = this.date$$.pipe(mergeMap(date => this.service.findByDate(date)));
  }
}
