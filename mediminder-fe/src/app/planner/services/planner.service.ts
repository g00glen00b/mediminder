import {inject, Injectable} from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {Observable} from 'rxjs';
import {MedicationPlan} from '../models/medication-plan';
import {Page} from '../../shared/models/page';
import {environment} from '../../../environment/environment';
import {format} from 'date-fns';
import {PageRequest, pageRequestToHttpParams} from '../../shared/models/page-request';

@Injectable({
  providedIn: 'root'
})
export class PlannerService {
  private readonly httpClient = inject(HttpClient);

  findAll(targetDate: Date, pageRequest: PageRequest): Observable<Page<MedicationPlan>> {
    console.log(targetDate);
    const formattedDate = format(targetDate, 'yyyy-MM-dd');
    const params = pageRequestToHttpParams(pageRequest);
    return this.httpClient.get<Page<MedicationPlan>>(`${environment.apiUrl}/planner/${formattedDate}`, {params});
  }
}
