import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Department } from './employee';

@Injectable({
  providedIn: 'root',
})
export class DepartmentService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/departments';

  getDepartments(): Observable<Department[]> {
    return this.http.get<Department[]>(this.apiUrl);
  }

  getDepartmentById(id: number): Observable<Department> {
    return this.http.get<Department>(`${this.apiUrl}/${id}`);
  }

  addDepartment(dept: Department): Observable<Department> {
    return this.http.post<Department>(this.apiUrl, dept);
  }

  updateDepartment(id: number, dept: Department): Observable<Department> {
    return this.http.put<Department>(`${this.apiUrl}/${id}`, dept);
  }

  deleteDepartment(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // Am făcut-o publică pentru a fi folosită și în componentă la nevoie
  public getEmailFromToken(): string {
    const token = localStorage.getItem('token');
    if (!token) return '';

    try {
      const payload = token.split('.')[1]; 
      const decodedPayload = JSON.parse(atob(payload)); 
      return decodedPayload.sub || decodedPayload.email || ''; 
    } catch (e) {
      console.error('Token invalid', e);
      return '';
    }
  }

  getMyDepartment(): Observable<Department> {
    const email = this.getEmailFromToken();
    return this.http.get<Department>(`${this.apiUrl}/my-department`, {
      headers: { 'User-Email': email }
    });
  }

  // În department.service.ts
updateDescription(email: string, description: string): Observable<Department> {
  return this.http.patch<Department>(
    `${this.apiUrl}/my-department/description`, 
    { description: description }, // TRIMITEM OBIECT, NU STRING
    { 
      headers: { 
        'User-Email': email,
        'Content-Type': 'application/json' // Forțăm header-ul
      } 
    }
  );
}
}