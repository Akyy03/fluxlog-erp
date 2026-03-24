import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Department {
  id?: number;
  name: string;
  description?: string;
  manager?: Employee; 
  employees?: Employee[];
}

export interface Employee {
  id?: number;
  firstName: string;
  lastName: string;
  position: string;
  salary: number;
  email: string;
  phone: string;
  hireDate: string;
  role?: 'EMPLOYEE' | 'MANAGER';
  departmentName?: string;
  department?: Department;
  temporaryPassword?: string;
  isDeleted?: boolean;
  isActive?: boolean;
}

@Injectable({
  providedIn: 'root',
})
export class EmployeeService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/employees';
  private deptUrl = 'http://localhost:8080/api/departments';

  getEmployees(): Observable<Employee[]> {
    return this.http.get<Employee[]>(this.apiUrl);
  }

  getDepartments(): Observable<Department[]> {
    return this.http.get<Department[]>(this.deptUrl);
  }

  addEmployee(employee: Employee): Observable<Employee> {
    return this.http.post<Employee>(this.apiUrl, employee);
  }

  updateEmployee(id: number, employee: Employee): Observable<Employee> {
    return this.http.put<Employee>(`${this.apiUrl}/${id}`, employee);
  }

  deleteEmployee(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getEmployeeById(id: number): Observable<Employee> {
    return this.http.get<Employee>(`${this.apiUrl}/${id}`);
  }

  restoreEmployee(id: number): Observable<Employee> {
    return this.http.put<Employee>(`${this.apiUrl}/${id}/restore`, {});
  }

  getMyProfile(email: string): Observable<Employee> {
    return this.http.get<Employee>(`${this.apiUrl}/me/${email}`);
  }

  updateMyProfile(email: string, data: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/me/update/${email}`, data);
  }
}
