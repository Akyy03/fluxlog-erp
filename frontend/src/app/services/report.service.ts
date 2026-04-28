import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class ReportService {
  private http = inject(HttpClient);
  private baseUrl = 'http://localhost:8080/api/reports';

  downloadManagerReport(): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/export/my-team`, { responseType: 'blob' });
  }

  downloadAdminReport(): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/export/master`, { responseType: 'blob' });
  }

  downloadPdfManager(): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/pdf/manager`, { responseType: 'blob' });
  }

  downloadPdfAdmin(): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/pdf/admin`, { responseType: 'blob' });
  }
}
