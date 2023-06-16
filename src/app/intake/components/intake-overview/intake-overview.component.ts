import {Component, OnInit} from '@angular/core';
import {IntakeService} from '../../services/intake.service';
import {BehaviorSubject, mergeMap, Observable} from 'rxjs';
import {Intake} from '../../models/intake';
import {ToastrService} from 'ngx-toastr';
import {Router} from "@angular/router";
import {addDays, subDays} from "date-fns";
import { AsyncPipe } from '@angular/common';
import { SwipeGestureDirective } from '../../../shared/directives/swipe-gesture.directive';
import { IntakeListComponent } from '../intake-list/intake-list.component';
import { DatePaginatorComponent } from '../../../shared/components/date-paginator/date-paginator.component';

@Component({
  selector: 'mediminder-intake-overview',
  templateUrl: './intake-overview.component.html',
  styleUrls: ['./intake-overview.component.scss'],
  standalone: true,
  imports: [
    DatePaginatorComponent,
    IntakeListComponent,
    SwipeGestureDirective,
    AsyncPipe
  ]
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

  onSwipeRight(): void {
    this.date$$.next(subDays(this.date$$.value, 1));
  }

  onSwipeLeft(): void {
    this.date$$.next(addDays(this.date$$.value, 1));
  }

  private initializeIntakes() {
    this.intakes$ = this.date$$.pipe(mergeMap(date => this.service.findByDate(date)));
  }
}
