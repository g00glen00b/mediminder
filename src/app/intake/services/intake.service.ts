import {Injectable} from '@angular/core';
import {ScheduleService} from "../../schedule/services/schedule.service";
import {
  combineLatest,
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
import {add, addDays, Duration, isAfter, isBefore, isEqual, isSameDay, parse, set} from "date-fns";
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
        filter(schedule => schedule.period.startingAt <= givenDateAtMidnight),
        filter(schedule => schedule.period.endingAtInclusive == null || schedule.period.endingAtInclusive >= givenDateAtMidnight),
        mergeMap(schedule => combineLatest([
          this.findAllCompletedBySchedule(schedule),
          this.findAllIntakeDatesUntil(schedule, givenDateAtMidnight)
        ]).pipe(
          filter(([, date]) => isSameDay(date, givenDateAtMidnight)),
          map(([completedEntities, scheduledDate]) => this.mapCompletedEntitiesAndScheduleInfoToIntake(completedEntities, schedule, scheduledDate)))),
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

  private mapCompletedEntitiesAndScheduleInfoToIntake(completedEntities: CompletedIntakeEntity[], schedule: Schedule, scheduledDate: Date): Intake {
    const completedEntity: CompletedIntakeEntity | undefined = completedEntities.find(({scheduledDate: completedScheduledDate}) => isEqual(completedScheduledDate, scheduledDate));
    if (completedEntity == null) {
      return {completed: null, schedule, scheduledDate};
    } else {
      const {completedDate, id} = completedEntity;
      const completed: CompletedIntake = {completedDate, id};
      return {completed, schedule, scheduledDate};
    }
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

}
