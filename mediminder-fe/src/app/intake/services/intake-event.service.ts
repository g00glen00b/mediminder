import {inject, Injectable} from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {format} from 'date-fns';
import {IntakeEvent} from '../models/intake-event';
import {environment} from '../../../environment/environment';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class IntakeEventService {
  private readonly httpClient = inject(HttpClient);

  findAll(targetDate: Date) {
    const targetDateString = format(targetDate, 'yyyy-MM-dd');
    return this.httpClient.get<IntakeEvent[]>(`./api/event/${targetDateString}`);
  }

  complete(scheduleId: string, targetDate: Date): Observable<IntakeEvent> {
    const targetDateString = format(targetDate, 'yyyy-MM-dd');
    return this.httpClient.post<IntakeEvent>(`./api/schedule/${scheduleId}/event/${targetDateString}`, null);
  }

  delete(id: string) {
    return this.httpClient.delete<void>(`./api/event/${id}`);
  }
}
