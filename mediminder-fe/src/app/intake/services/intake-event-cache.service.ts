import {inject, Injectable} from '@angular/core';
import {IntakeEvent} from '../models/intake-event';
import {IntakeEventService} from './intake-event.service';
import {Observable, of, tap} from 'rxjs';
import {addDays, DateArg, format, subDays} from 'date-fns';

@Injectable({
  providedIn: 'root'
})
export class IntakeEventCacheService {
  private readonly service = inject(IntakeEventService);
  private cache = new Map<string, IntakeEvent[]>();

  findAll(targetDate: Date, prefetchAdjacent: boolean = true): Observable<IntakeEvent[]> {
    const key = this.getCacheKey(targetDate);
    let result: Observable<IntakeEvent[]>;
    if (this.cache.has(key)) {
      result = of(this.cache.get(key)!);
    } else {
      result = this.findAllAndCache(targetDate);
    }
    if (prefetchAdjacent) this.prefetchAdjacentDates(targetDate);
    return result;
  }

  complete(event: IntakeEvent): Observable<IntakeEvent> {
    const key = this.getCacheKey(event.targetDate);
    return this.service.complete(event).pipe(tap(resultEvent => {
      if (this.cache.has(key)) {
        const events = this.cache.get(key)!;
        const results = events.map(currentEvent => currentEvent === event ? resultEvent : currentEvent);
        this.cache.set(key, results);
      }
    }));
  }

  deleteFromCache(targetDate: Date): void {
    const key = this.getCacheKey(targetDate);
    this.cache.delete(key);
  }

  private prefetchAdjacentDates(targetDate: Date): void {
    const previousDate = subDays(targetDate, 1);
    const nextDate = addDays(targetDate, 1);
    this.findAll(previousDate, false).subscribe();
    this.findAll(nextDate, false).subscribe();
  }

  private findAllAndCache(targetDate: Date): Observable<IntakeEvent[]> {
    const key = this.getCacheKey(targetDate);
    return this.service.findAll(targetDate).pipe(
      tap(events => this.cache.set(key, events))
    );
  }

  private getCacheKey(targetDate: DateArg<Date>): string {
    return format(targetDate, 'yyyy-MM-dd');
  }
}
