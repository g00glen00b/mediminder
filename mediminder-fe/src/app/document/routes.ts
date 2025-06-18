import {HasAuthority, IsLoggedIn} from '../user/guards';
import {Route} from '@angular/router';
import {CreateDocumentPageComponent} from './pages/create-document-page/create-document-page.component';
import {EditDocumentPageComponent} from './pages/edit-document-page/edit-document-page.component';

export default [
  {
    path: 'create',
    component: CreateDocumentPageComponent,
    canActivate: [IsLoggedIn, HasAuthority('Document')],
  },
  {
    path: ':id/duplicate',
    component: CreateDocumentPageComponent,
    canActivate: [IsLoggedIn, HasAuthority('Document')],
  },
  {
    path: ':id/edit',
    component: EditDocumentPageComponent,
    canActivate: [IsLoggedIn, HasAuthority('Document')],
  }
] as Route[];
