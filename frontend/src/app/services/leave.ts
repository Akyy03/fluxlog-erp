import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class LeaveService {
  private apiUrl = 'http://localhost:8080/api/leave-requests';

  constructor(private http: HttpClient) {}

  createRequest(employeeId: number, request: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/employee/${employeeId}`, request);
  }

  getMyRequests(employeeId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/employee/${employeeId}`);
  }

  getDepartmentRequests(deptId: number): Observable<any[]> {
  return this.http.get<any[]>(`${this.apiUrl}/department/${deptId}`);
}

  getAllRequests(): Observable<any[]> {
  return this.http.get<any[]>(this.apiUrl); 
}

  deleteRequest(id: number): Observable<void> {
  return this.http.delete<void>(`${this.apiUrl}/${id}`);
}

// Pentru Manager (doar cele care așteaptă aprobare)
getPendingDepartmentRequests(deptId: number): Observable<any[]> {
  return this.http.get<any[]>(`${this.apiUrl}/department/${deptId}/pending`);
}

updateRequestStatus(id: number, status: string, comment: string): Observable<any> {
  // Construim parametrii de URL
  const params = new HttpParams()
    .set('status', status)
    .set('comment', comment || '');

  return this.http.put(`${this.apiUrl}/${id}/status`, null, { params });
}

getEmployeeProfile(id: number): Observable<any> {
return this.http.get(`http://localhost:8080/api/employees/${id}`);}
}