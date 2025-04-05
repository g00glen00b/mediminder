import {inject, Injectable} from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {PageRequest, pageRequestToHttpParams} from '../../shared/models/page-request';
import {Observable} from 'rxjs';
import {Schedule} from '../models/schedule';
import {Page} from '../../shared/models/page';
import {environment} from '../../../environment/environment';
import {CreateScheduleRequest} from '../models/create-schedule-request';
import {UpdateScheduleRequest} from '../models/update-schedule-request';

@Injectable({
  providedIn: 'root'
})
export class ScheduleService {
  private readonly httpClient = inject(HttpClient);

  findAll(pageRequest: PageRequest, medicationId?: string, onlyActive: boolean = false): Observable<Page<Schedule>> {
    let params = pageRequestToHttpParams(pageRequest);
    if (medicationId != null) params = params.set('medicationId', medicationId);
    if (onlyActive) params = params.set('onlyActive', onlyActive);
    return this.httpClient.get<Page<Schedule>>(`${environment.apiUrl}/schedule`, {params});
  }

  findById(id: string): Observable<Schedule> {
    return this.httpClient.get<Schedule>(`${environment.apiUrl}/schedule/${id}`);
  }

  create(request: CreateScheduleRequest): Observable<Schedule> {
    return this.httpClient.post<Schedule>(`${environment.apiUrl}/schedule`, request);
  }

  update(id: string, request: UpdateScheduleRequest): Observable<Schedule> {
    return this.httpClient.put<Schedule>(`${environment.apiUrl}/schedule/${id}`, request);
  }

  delete(id: string): Observable<void> {
    return this.httpClient.delete<void>(`${environment.apiUrl}/schedule/${id}`);
  }
}
