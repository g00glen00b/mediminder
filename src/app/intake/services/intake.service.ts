import {Injectable} from '@angular/core';
import {ScheduleService} from "../../schedule/services/schedule.service";
import {
  combineLatest, defaultIfEmpty,
  filter,
  from,
  generate,
  groupBy,
  map,
  mergeMap,
  Observable,
  reduce,
  take,
  tap,
  toArray
} from "rxjs";
import {Intake} from "../models/intake";
import {Schedule} from "../../schedule/models/schedule";
import {add, addDays, differenceInDays, Duration, isAfter, isBefore, isEqual, isSameDay, parse, set} from "date-fns";
import {ScheduleRecurrence} from "../../schedule/models/schedule-recurrence";
import {compareByField} from "../../shared/utils/compare-utils";
import {MIDNIGHT} from "../../shared/utils/date-fns-utils";
import {NgxIndexedDBService} from "ngx-indexed-db";
import {CompletedIntakeEntity} from "../models/completed-intake-entity";
import {CompletedIntake} from "../models/completed-intake";
import {TotalIntakeDose} from "../models/total-intake-dose";
import {MediminderEventService} from "../../shared/services/mediminder-event.service";
import {IntakeCompletedEvent} from "../models/intake-completed-event";
import {da} from "date-fns/locale";
import {ScheduleRecurrenceType} from "../../schedule/models/schedule-recurrence-type";

const STORE_NAME = 'intake';

@Injectable({
  providedIn: 'root'
})
export class IntakeService {

  constructor(
    private scheduleService: ScheduleService,
    private dbService: NgxIndexedDBService,
    private eventService: MediminderEventService) { }

  findByDate(givenDate: Date): Observable<Intake[]> {
    const givenDateAtMidnight: Date = set(givenDate, MIDNIGHT);
    return this.scheduleService
      .findAll({field: 'name', direction: 'asc'})
      .pipe(
        mergeMap(schedules => from(schedules)),
        filter(schedule => this.isActiveAtDate(schedule, givenDateAtMidnight)),
        mergeMap(schedule => this.mapScheduleToIntake(schedule, givenDateAtMidnight)),
        toArray(),
        map(results => results.sort(compareByField(result => result.scheduledDate))));
  }

  complete(intake: Intake): Observable<Intake> {
    const {scheduledDate, schedule: {id: scheduleId}} = intake;
    const id: string = crypto.randomUUID();
    const completedDate: Date = new Date();
    const entity: CompletedIntakeEntity = {id, completedDate, scheduledDate, scheduleId};
    return this.dbService
      .add(STORE_NAME, entity)
      .pipe(
        take(1),
        map(() => ({...intake, completedDate})),
        tap(intake => this.eventService.publish(new IntakeCompletedEvent(intake.schedule.medication.id, intake.schedule.dose))));
  }

  findTotalIntakeDosesUntil(date: Date): Observable<TotalIntakeDose[]> {
    const todayAtMidnight: Date = set(new Date(), MIDNIGHT);
    const dateAtMidnight: Date = set(date, MIDNIGHT);
    return generate({
      initialState: todayAtMidnight,
      condition: date => !isAfter(date, dateAtMidnight),
      iterate: date => addDays(date, 1)
    }).pipe(
      mergeMap(date => this.findByDate(date)),
      mergeMap(intakes => from(intakes)),
      groupBy(intake => intake.schedule.medication.id),
      mergeMap(group => group
        .pipe(
          map(intake => intake.schedule.dose),
          reduce((totalDose, dose) => totalDose + dose, 0),
          map(totalDose => ({totalDose, medicationId: group.key})))),
      toArray());
  }

  private mapScheduleToIntake(schedule: Schedule, date: Date): Observable<Intake> {
    const scheduledDate = parse(schedule.time, 'HH:mm', date);
    return this
      .findAllCompletedBySchedule(schedule)
      .pipe(
        mergeMap(entities => from(entities)),
        filter(entity => isEqual(entity.scheduledDate, scheduledDate)),
        take(1),
        map(({completedDate, id}) => ({completedDate, id}) as CompletedIntake),
        defaultIfEmpty(null),
        map(completed => ({completed, schedule, scheduledDate})));
  }

  private findAllCompletedBySchedule(schedule: Schedule): Observable<CompletedIntakeEntity[]> {
    return this.dbService
      .getAllByIndex<CompletedIntakeEntity>(STORE_NAME, 'scheduleId', IDBKeyRange.only(schedule.id))
      .pipe(take(1));
  }

  private findAllIntakeDatesUntil(schedule: Schedule, givenDate: Date): Observable<Date> {
    const startDateAtScheduleTime: Date = parse(schedule.time, 'HH:mm', schedule.period.startingAt);
    const givenDateAtScheduleTime: Date = parse(schedule.time, 'HH:mm', givenDate);
    const endDateAtScheduleTime: Date | null = schedule.period.endingAtInclusive == null ? null : parse(schedule.time, 'HH:mm', schedule.period.endingAtInclusive);
    const actualEndDate: Date = endDateAtScheduleTime == null || isBefore(givenDateAtScheduleTime, endDateAtScheduleTime) ? givenDateAtScheduleTime : endDateAtScheduleTime;
    const duration: Duration = this.calculateDuration(schedule.recurrence);

    return generate({
      initialState: startDateAtScheduleTime,
      condition: date => !isAfter(date, actualEndDate),
      iterate: date => add(date, duration)
    });
  }

  private calculateDuration(recurrence: ScheduleRecurrence): Duration {
    switch (recurrence.type) {
      case 'daily': return {days: recurrence.units};
      case 'weekly': return {weeks: recurrence.units};
    }
  }

  private isActiveAtDate(schedule: Schedule, date: Date): boolean {
    const {period: {startingAt, endingAtInclusive}, recurrence} = schedule;
    if (isAfter(startingAt, date)) return false;
    if (endingAtInclusive != null && isBefore(endingAtInclusive, date)) return false;
    const days = this.calculateExpectedDaysInRecurrence(recurrence);
    const startingAtMidnight: Date = set(startingAt, MIDNIGHT);
    return differenceInDays(date, startingAtMidnight) % days === 0;
  }

  private calculateExpectedDaysInRecurrence(recurrence: ScheduleRecurrence): number {
    switch (recurrence.type) {
      case 'daily': return recurrence.units;
      case 'weekly': return recurrence.units * 7;
    }
  }
}
