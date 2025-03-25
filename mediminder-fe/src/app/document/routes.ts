import {IsLoggedIn} from '../user/guards';
import {Route} from '@angular/router';
import {DocumentOverviewPageComponent} from './pages/document-overview-page/document-overview-page.component';
import {CreateDocumentPageComponent} from './pages/create-document-page/create-document-page.component';
import {EditDocumentPageComponent} from './pages/edit-document-page/edit-document-page.component';

export default [
  {
    path: '',
    component: DocumentOverviewPageComponent,
    canActivate: [IsLoggedIn],
  },
  {
    path: 'create',
    component: CreateDocumentPageComponent,
    canActivate: [IsLoggedIn],
  },
  {
    path: ':id/duplicate',
    component: CreateDocumentPageComponent,
    canActivate: [IsLoggedIn],
  },
  {
    path: ':id/edit',
    component: EditDocumentPageComponent,
    canActivate: [IsLoggedIn],
  }
] as Route[];
