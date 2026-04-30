import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Project {
  id: number;
  name: string;
  description: string;
  deadline: string;
  status: 'ACTIVE' | 'ON_HOLD' | 'COMPLETED';
  progress: number;
  createdByUserName: string;
  departmentIds: number[];
}

@Injectable({
  providedIn: 'root'
})
export class ProjectService {
  private apiUrl = 'http://localhost:8080/api/projects';

  constructor(private http: HttpClient) { }

  getProjects(): Observable<Project[]> {
    return this.http.get<Project[]>(this.apiUrl);
  }

  createProject(project: any): Observable<Project> {
  return this.http.post<Project>(this.apiUrl, project);
}

getActiveProjectsCount(deptId: number): Observable<number> {
  return this.http.get<number>(`${this.apiUrl}/count/active/${deptId}`);
}
}