export interface Page<T> {
  content: T[];
  pageable: Pageable;
  first: boolean;
  last: boolean;
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  sort: PageableSort;
  numberOfElements: number;
  empty: boolean;
}

export interface Pageable {
  pageNumber: number;
  pageSize: number;
  sort: PageableSort;
  offset: number;
  paged: boolean;
  unpaged: boolean;
}

export interface PageableSort {
  empty: boolean;
  sorted: boolean;
  unsorted: boolean;
}

export function emptyPage<T>(): Page<T> {
  return {
    content: [],
    empty: true,
    first: true,
    last: true,
    number: 0,
    numberOfElements: 0,
    pageable: {
      paged: true,
      offset: 0,
      pageNumber: 0,
      pageSize: 0,
      sort: {
        empty: true,
        sorted: true,
        unsorted: false,
      },
      unpaged: false,
    },
    size: 0,
    sort: {
      empty: true,
      sorted: true,
      unsorted: false,
    },
    totalElements: 0,
    totalPages: 1,
  };
}
