import {CreateDocumentRequest} from './create-document-request';

export interface CreateDocumentRequestWrapper {
  request: CreateDocumentRequest;
  file?: File;
}
