import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

// Interfața Department rămâne utilă pentru dropdown-uri și detalii
export interface Department {
  id?: number;
  name: string;
  description?: string;
  manager?: any;
  employees?: any[];
}

export interface Employee {
  id?: number;
  firstName: string;
  lastName: string;
  email: string;
  position: string;
  salary: number;
  phone: string;
  departmentId?: number;    
  departmentName?: string;  
  hireDate: string;
  role?: string;      
  temporaryPassword?: string;
  isDeleted?: boolean;
  remainingLeaveDays?: number;  
  
  department?: Department;
  isActive?: boolean;
}

@Injectable({
  providedIn: 'root',
})
export class EmployeeService {
  private http = inject(HttpClient);
  
  private apiUrl = 'http://localhost:8080/api/employees';
  private deptUrl = 'http://localhost:8080/api/departments';

  // --- EMPLOYEE CRUD ---

  getEmployees(): Observable<Employee[]> {
    return this.http.get<Employee[]>(this.apiUrl);
  }

  getEmployeeById(id: number): Observable<Employee> {
    return this.http.get<Employee>(`${this.apiUrl}/${id}`);
  }

  addEmployee(employee: Employee): Observable<Employee> {
    // Trimitere POST către /api/employees
    return this.http.post<Employee>(this.apiUrl, employee);
  }

  updateEmployee(id: number, employee: Employee): Observable<Employee> {
    // Trimitere PUT către /api/employees/{id}
    return this.http.put<Employee>(`${this.apiUrl}/${id}`, employee);
  }

  deleteEmployee(id: number): Observable<void> {
    // Trimitere DELETE către /api/employees/{id}
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  restoreEmployee(id: number): Observable<Employee> {
    // Trimitere PUT către /api/employees/{id}/restore
    return this.http.put<Employee>(`${this.apiUrl}/${id}/restore`, {});
  }

  // --- PROFILUL MEU (Logica specifică utilizatorului logat) ---

  getMyProfile(email: string): Observable<Employee> {
    return this.http.get<Employee>(`${this.apiUrl}/me/${email}`);
  }

  updateMyProfile(email: string, data: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/me/update/${email}`, data);
  }

  // --- DEPARTMENTS ---

  getDepartments(): Observable<Department[]> {
    return this.http.get<Department[]>(this.deptUrl);
  }
}