import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReportService } from '../../services/report.service';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './reports.html',
  styleUrls: ['./reports.css'],
})
export class Reports {
  private reportService = inject(ReportService);
  loading = signal(false);

  get userRole(): string {
    return localStorage.getItem('role') || '';
  }

  get roleDescription(): string {
    return this.isAdmin()
      ? 'Generate and download system-wide exports for HR and Project analytics.'
      : 'Generate and download team-specific reports for department performance tracking.';
  }

  isManager(): boolean {
    return this.userRole === 'MANAGER';
  }
  isAdmin(): boolean {
    return this.userRole === 'ADMIN';
  }

  onDownload(type: 'MANAGER' | 'ADMIN') {
    this.loading.set(true);
    const obs =
      type === 'MANAGER'
        ? this.reportService.downloadManagerReport()
        : this.reportService.downloadAdminReport();

    obs.subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `Raport_${type}_${new Date().getTime()}.xlsx`;
        a.click();
        window.URL.revokeObjectURL(url);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        alert('Eroare la descărcarea raportului.');
      },
    });
  }

  onDownloadPdf(type: 'MANAGER' | 'ADMIN') {
    if (type === 'MANAGER') {
      this.reportService.downloadPdfManager().subscribe((blob) => {
        this.downloadFile(blob, 'Raport_Echipa.pdf');
      });
    } else {
      this.reportService.downloadPdfAdmin().subscribe((blob) => {
        this.downloadFile(blob, 'Raport_Master.pdf');
      });
    }
  }

  // Metoda utilitară care declanșează descărcarea
  private downloadFile(blob: Blob, filename: string) {
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    a.click();
    window.URL.revokeObjectURL(url);
  }
}
