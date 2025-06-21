import {HasAuthority, IsAuthenticated} from '../user/guards';
import {Route} from '@angular/router';
import {CreateDocumentPageComponent} from './pages/create-document-page/create-document-page.component';
import {EditDocumentPageComponent} from './pages/edit-document-page/edit-document-page.component';

export default [
  {
    path: 'create',
    component: CreateDocumentPageComponent,
    canActivate: [IsAuthenticated(), HasAuthority('Document')],
  },
  {
    path: ':id/duplicate',
    component: CreateDocumentPageComponent,
    canActivate: [IsAuthenticated(), HasAuthority('Document')],
  },
  {
    path: ':id/edit',
    component: EditDocumentPageComponent,
    canActivate: [IsAuthenticated(), HasAuthority('Document')],
  }
] as Route[];
