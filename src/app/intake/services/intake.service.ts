import {Injectable} from '@angular/core';
import {ScheduleService} from "../../schedule/services/schedule.service";
import {defaultIfEmpty, filter, from, groupBy, map, mergeMap, Observable, reduce, take, tap, toArray} from "rxjs";
import {Intake} from "../models/intake";
import {Schedule} from "../../schedule/models/schedule";
import {differenceInDays, isAfter, isBefore, isEqual, parse, set} from "date-fns";
import {ScheduleRecurrence} from "../../schedule/models/schedule-recurrence";
import {compareByField} from "../../shared/utils/compare-utils";
import {MIDNIGHT} from "../../shared/utils/date-fns-utils";
import {NgxIndexedDBService} from "ngx-indexed-db";
import {CompletedIntakeEntity} from "../models/completed-intake-entity";
import {CompletedIntake} from "../models/completed-intake";
import {TotalIntakeDose} from "../models/total-intake-dose";
import {MediminderEventService} from "../../shared/services/mediminder-event.service";
import {IntakeCompletedEvent} from "../models/intake-completed-event";

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
    const dateAtMidnight: Date = set(date, MIDNIGHT);
    return this.scheduleService
      .findAll({field: 'name', direction: 'asc'})
      .pipe(
        mergeMap(schedules => from(schedules)),
        map(schedule => this.mapScheduleToIntakeDose(schedule, dateAtMidnight)),
        groupBy(total => total.medicationId),
        mergeMap(group => group.pipe(
          map(group => group.totalDose),
          reduce((result, dose) => result + dose, 0),
          map(totalDose => ({totalDose, medicationId: group.key})))),
        filter(total => total.totalDose > 0),
        toArray());
  }

  private mapScheduleToIntakeDose(schedule: Schedule, date: Date): TotalIntakeDose {
    const {medication: {id: medicationId}, recurrence, period: {startingAt, endingAtInclusive}} = schedule;
    const todayAtMidnight: Date = set(new Date(), MIDNIGHT);
    const startDate = isBefore(startingAt, todayAtMidnight) ? todayAtMidnight : startingAt;
    const endDate = endingAtInclusive == null || isBefore(date, endingAtInclusive) ? date : endingAtInclusive;
    const days = differenceInDays(endDate, startingAt);
    const alreadyPassedDays = differenceInDays(startDate, startingAt);
    if (days <= 0) return {medicationId, totalDose: 0};
    const expectedDays = this.calculateExpectedDaysInRecurrence(recurrence);
    const totalDose = Math.floor(days / expectedDays) * schedule.dose;
    const alreadyPassedDose = Math.floor(alreadyPassedDays / expectedDays) * schedule.dose;
    return {medicationId, totalDose: totalDose - alreadyPassedDose};
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
