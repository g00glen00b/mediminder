import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {MedicationType} from '../models/medication-type';
import {defaultPageRequest, pageRequestToHttpParams} from '../../shared/models/page-request';
import {Page} from '../../shared/models/page';
import {environment} from '../../../environment/environment';
import {DoseType} from '../models/dose-type';
import {AdministrationType} from '../models/administration-type';

@Injectable({
  providedIn: 'root'
})
export class MedicationTypeService {
  private readonly httpClient = inject(HttpClient);

  findAll(pageRequest = defaultPageRequest()): Observable<Page<MedicationType>> {
    const params = pageRequestToHttpParams(pageRequest);
    return this.httpClient.get<Page<MedicationType>>(`${environment.apiUrl}/medication-type`, {params});
  }

  findAllDoseTypes(medicationTypeId: string, pageRequest = defaultPageRequest()): Observable<Page<DoseType>> {
    const params = pageRequestToHttpParams(pageRequest);
    return this.httpClient.get<Page<DoseType>>(`${environment.apiUrl}/medication-type/${medicationTypeId}/dose-type`, {params});
  }

  findAllAdministrationTypes(medicationTypeId: string, pageRequest = defaultPageRequest()): Observable<Page<AdministrationType>> {
    const params = pageRequestToHttpParams(pageRequest);
    return this.httpClient.get<Page<AdministrationType>>(`${environment.apiUrl}/medication-type/${medicationTypeId}/administration-type`, {params});
  }
}
