export interface Page<T> {
  content: T[];
  page: PageInfo;
}

export interface PageInfo {
  size: number;
  number: number;
  totalElements: number;
  totalPages: number;
}

export function emptyPage<T>(): Page<T> {
  return {
    content: [],
    page: {
      size: 0,
      number: 0,
      totalElements: 0,
      totalPages: 0,
    }
  }
}
