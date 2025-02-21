import { HttpParams } from '@angular/common/http';

export interface PageRequest {
  page?: number;
  size?: number;
  sort?: readonly string[];
}

export function defaultPageRequest(sort: string[] = []): PageRequest {
  return {page: 0, size: 20, sort};
}

export function pageRequestToHttpParams(request: PageRequest): HttpParams {
  const values = JSON.parse(JSON.stringify(request));
  return new HttpParams({fromObject: values});
}
