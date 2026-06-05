import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class InvoiceService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/invoices';

  generateInvoicePdf(requestData: any): Observable<Blob> {
    return this.http.post(`${this.apiUrl}/generate`, requestData, {
      responseType: 'blob'
    });
  }
}