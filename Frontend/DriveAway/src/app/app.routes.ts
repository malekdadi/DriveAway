import { Routes } from '@angular/router';
import { DrivePage, services } from './drive-page';

export const routes: Routes = [
  { path: '', component: DrivePage, data: { page: 'accueil' }, title: 'DriveAway — Location de voitures' },
  ...services.map(service => ({
    path: service.path,
    component: DrivePage,
    data: { page: service.path },
    title: service.title + ' | DriveAway',
  })),
  { path: '**', redirectTo: '' },
];
