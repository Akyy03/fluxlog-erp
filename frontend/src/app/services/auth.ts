import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = 'http://localhost:8080/api/v1/auth';

  private router = inject(Router);

  constructor(private http: HttpClient) {}

  login(credentials: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/login`, credentials).pipe(
      tap((response: any) => {
        localStorage.setItem('token', response.token);
        localStorage.setItem('role', response.role);
        localStorage.setItem('email', response.email);
        localStorage.setItem('userId', String(response.userId));
        localStorage.setItem(
          'deptId',
          response.departmentId ? response.departmentId.toString() : '0',
        );
        localStorage.setItem('needsPasswordChange', String(response.needsPasswordChange));
      }),
    );
  }

  logout() {
    localStorage.clear();
    sessionStorage.clear();

    window.location.href = '/login';
  }

  isLoggedIn(): boolean {
    const token = localStorage.getItem('token');
    return !!token && token !== 'null' && token !== 'undefined';
  }

  getUserId(): number | null {
    const userId = localStorage.getItem('userId');
    return userId ? parseInt(userId, 10) : null;
  }

  getRole(): string | null {
    return localStorage.getItem('role');
  }

  isAdmin(): boolean {
    return this.getRole() === 'ADMIN';
  }

  isManager(): boolean {
    return this.getRole() === 'MANAGER';
  }

  isEmployee(): boolean {
    return this.getRole() === 'EMPLOYEE';
  }

  getUserEmail(): string {
    return localStorage.getItem('email') || '';
  }
}
