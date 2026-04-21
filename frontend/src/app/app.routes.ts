import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login';
import { AdminShell } from './components/layout/admin-shell/admin-shell';
import { EmployeeListComponent } from './components/employee-list/employee-list';
import { SetupPassword } from './auth/setup-password/setup-password';

import { authGuard } from './services/auth.guard';
import { guestGuard } from './services/guest.guard';
import { roleGuard } from './services/role.guard';

import { Dashboard } from './components/dashboard/dashboard';
import { ProjectsComponent } from './components/projects/projects';
import { LeavesComponent } from './components/leaves/leaves';
import { MyDepartmentComponent } from './components/my-department/my-department';
import { Reports } from './components/reports/reports';
import { ProjectDetailsComponent } from './components/project-details/project-details';

export const routes: Routes = [
  {
    path: 'login',
    component: LoginComponent,
    canActivate: [guestGuard],
  },
  {
    path: 'setup-password',
    component: SetupPassword,
    canActivate: [authGuard],
  },
  {
    path: '',
    component: AdminShell,
    canActivate: [authGuard],
    children: [
      {
        path: 'dashboard',
        component: Dashboard,
      },
      {
        path: 'employees',
        component: EmployeeListComponent,
        canActivate: [roleGuard],
        data: { roles: ['ADMIN', 'MANAGER'] },
      },
      {
        path: 'employees/:id',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN', 'MANAGER'] },
        loadComponent: () =>
          import('./features/employees/employee-detail/employee-detail').then(
            (m) => m.EmployeeDetailComponent,
          ),
      },
      {
        path: 'departments',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN'] },
        loadComponent: () =>
          import('./components/department-list/department-list').then((m) => m.DepartmentList),
      },
      {
        path: 'my-department',
        canActivate: [roleGuard],
        data: { roles: ['MANAGER'] },
        loadComponent: () =>
          import('./components/my-department/my-department').then((m) => m.MyDepartmentComponent),
      },
      {
        path: 'reports',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN', 'MANAGER'] },
        loadComponent: () => import('./components/reports/reports').then((m) => m.Reports),
      },
      {
        path: 'projects',
        component: ProjectsComponent,
        canActivate: [roleGuard],
        data: { roles: ['ADMIN', 'MANAGER'] },
      },
      {
        path: 'projects/:id',
        component: ProjectDetailsComponent,
        canActivate: [roleGuard],
        data: { roles: ['ADMIN', 'MANAGER'] },
      },
      {
        path: 'my-tasks',
        canActivate: [roleGuard],
        data: { roles: ['EMPLOYEE'] },
        loadComponent: () =>
          import('./components/my-tasks/my-tasks').then((m) => m.MyTasksComponent),
      },
      {
        path: 'leaves',
        component: LeavesComponent,
      },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
    ],
  },
  { path: '**', redirectTo: 'login' },
];
