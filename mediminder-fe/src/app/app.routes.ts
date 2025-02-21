import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'home',
    loadChildren: () => import('./home/routes'),
  },
  {
    path: 'user',
    loadChildren: () => import('./user/routes'),
  },
  {
    path: 'cabinet',
    loadChildren: () => import('./cabinet/routes'),
  },
  {
    path: 'medication',
    loadChildren: () => import('./medication/routes'),
  },
  {
    path: 'schedule',
    loadChildren: () => import('./schedule/routes'),
  },
  {
    path: 'planner',
    loadChildren: () => import('./planner/routes'),
  },
  {
    path: '',
    redirectTo: 'home',
    pathMatch: 'full'
  }
];
