import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {PageRequest, pageRequestToHttpParams} from '../../shared/models/page-request';
import {Observable} from 'rxjs';
import {Page} from '../../shared/models/page';
import {environment} from '../../../environment/environment';
import {CreateDocumentRequest} from '../models/create-document-request';
import {UpdateDocumentRequest} from '../models/update-document-request';
import {Document} from '../models/document';

@Injectable({
  providedIn: 'root'
})
export class DocumentService {
  private readonly httpClient = inject(HttpClient);

  findAll(pageRequest: PageRequest, medicationId?: string): Observable<Page<Document>> {
    let params = pageRequestToHttpParams(pageRequest);
    if (medicationId != null) params = params.set('medicationId', medicationId);
    return this.httpClient.get<Page<Document>>(`./api/document`, {params});
  }

  findById(id: string): Observable<Document> {
    return this.httpClient.get<Document>(`./api/document/${id}`);
  }

  create(request: CreateDocumentRequest, file: File): Observable<Document> {
    const data: FormData = new FormData();
    data.append('file', file);
    data.append('request', new Blob([JSON.stringify(request)], {type: 'application/json'}));
    return this.httpClient.post<Document>(`./api/document`, data);
  }

  update(id: string, request: UpdateDocumentRequest): Observable<Document> {
    return this.httpClient.put<Document>(`./api/document/${id}`, request);
  }

  delete(id: string): Observable<void> {
    return this.httpClient.delete<void>(`./api/document/${id}`);
  }

  download(doc: Document): void {
    this.httpClient
      .get<Blob>(`./api/document/${doc.id}/download`, {responseType: 'blob' as 'json'})
      .subscribe(blob => {
        const dataType = blob.type;
        let binaryData = [];
        binaryData.push(blob);
        const downloadLink = document.createElement('a');
        downloadLink.href = window.URL.createObjectURL(new Blob(binaryData, {type: dataType}));
        downloadLink.setAttribute('download', doc.filename);
        document.body.appendChild(downloadLink);
        downloadLink.click();
        URL.revokeObjectURL(downloadLink.href);
      });
  }
}
