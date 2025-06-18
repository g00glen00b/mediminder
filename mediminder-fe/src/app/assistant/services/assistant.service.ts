import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {AssistantRequest} from '../models/assistant-request';
import {Observable} from 'rxjs';
import {AssistantResponse} from '../models/assistant-response';
import {environment} from '../../../environment/environment';

@Injectable({
  providedIn: 'root'
})
export class AssistantService {
  private readonly httpClient = inject(HttpClient);

  ask(request: AssistantRequest): Observable<AssistantResponse> {
    return this.httpClient.post<AssistantResponse>(`./api/assistant`, request);
  }
}
