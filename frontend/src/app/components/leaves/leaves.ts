import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LeaveService } from '../../services/leave';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-leaves',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './leaves.html',
  styleUrls: ['./leaves.css'],
})
export class LeavesComponent implements OnInit {
  private cdr = inject(ChangeDetectorRef);
  private leaveService = inject(LeaveService);
  private authService = inject(AuthService);

  currentEmployeeId = Number(localStorage.getItem('userId'));
  deptId = Number(localStorage.getItem('deptId'));
  myEmail = localStorage.getItem('email');

  isAdmin = false;
  isManager = false;

  newRequest = { startDate: '', endDate: '', type: 'VACATION', reason: '' };

  remainingDays: number = 0;
  selectedWorkDays: number = 0;

  myRequests: any[] = [];
  teamRequests: any[] = [];
  showRejectInput: { [key: number]: boolean } = {};

  message: string = '';
  isSubmitting = false;
  minDate: string = '';

  ngOnInit(): void {
    this.isAdmin = this.authService.isAdmin();
    this.isManager = this.authService.isManager();
    this.currentEmployeeId = Number(localStorage.getItem('userId'));
    this.deptId = Number(localStorage.getItem('deptId'));
    this.myEmail = localStorage.getItem('email');

    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    this.minDate = tomorrow.toISOString().split('T')[0];

    this.loadData();
  }

  loadRemainingDays() {
    const profileId = this.currentEmployeeId - 1;

    this.leaveService.getEmployeeProfile(profileId).subscribe({
      next: (emp) => {
        if (emp && emp.remainingLeaveDays !== undefined) {
          this.remainingDays = emp.remainingLeaveDays;
          this.cdr.detectChanges();
        }
      },
      error: (err) => {
        console.error("Eroare la profile (ID " + profileId + "):", err);
      }
    });
  }

  loadData() {
    if (!this.isAdmin) {
      this.leaveService.getMyRequests(this.currentEmployeeId).subscribe({
        next: (data) => {
          this.myRequests = (data || []).sort((a: any, b: any) => b.id - a.id);
          this.loadRemainingDays();
          this.cdr.detectChanges();
        }
      });
    }

    if (this.isAdmin) {
      this.loadAllRequests();
    } else if (this.isManager) {
      this.leaveService.getDepartmentRequests(this.deptId).subscribe({
        next: (data) => {
          this.teamRequests = (data || []).sort((a: any, b: any) => b.id - a.id);
          this.cdr.detectChanges();
        }
      });
    }
  }

  submitRequest() {
    if (!this.newRequest.startDate || !this.newRequest.endDate) {
      this.message = 'Te rugăm să selectezi perioada.';
      return;
    }

    this.isSubmitting = true;
    this.leaveService.createRequest(this.currentEmployeeId, this.newRequest).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.message = 'Succes! Cerere trimisă.';
        this.resetForm();
        this.loadData();
      },
      error: (err) => {
        this.isSubmitting = false;
        this.message = 'Eroare: ' + (err.error?.message || 'Eroare server.');
      },
    });
  }

  private updateStatus(id: number, status: 'APPROVED' | 'REJECTED', comment: string = '') {
    this.isSubmitting = true;
    this.leaveService.updateRequestStatus(id, status, comment).subscribe({
      next: () => {
        this.message = `Cerere ${status === 'APPROVED' ? 'aprobată' : 'respinsă'}.`;
        this.isSubmitting = false;
        this.loadData();
      },
      error: (err) => {
        this.message = 'Eroare: ' + (err.error?.message || 'Eroare la update.');
        this.isSubmitting = false;
        this.cdr.detectChanges();
      }
    });
  }

  calculateWorkDays() {
    if (!this.newRequest.startDate || !this.newRequest.endDate) {
      this.selectedWorkDays = 0;
      return;
    }
    let start = new Date(this.newRequest.startDate);
    let end = new Date(this.newRequest.endDate);
    let count = 0;
    let cur = new Date(start);
    while (cur <= end) {
      const dayOfWeek = cur.getDay();
      if (dayOfWeek !== 0 && dayOfWeek !== 6) count++;
      cur.setDate(cur.getDate() + 1);
    }
    this.selectedWorkDays = count;
  }

  private resetForm() {
    this.newRequest = { startDate: '', endDate: '', type: 'VACATION', reason: '' };
    this.selectedWorkDays = 0;
  }

  deleteRequest(id: number) {
    if (confirm('Ștergi cererea?')) {
      this.leaveService.deleteRequest(id).subscribe({
        next: () => {
          this.message = 'Cerere ștearsă.';
          this.loadData();
        }
      });
    }
  }

  toggleRejectBox(id: number) {
    this.showRejectInput[id] = !this.showRejectInput[id];
  }

  approveRequest(id: number) {
    const comment = prompt('Comentariu (opțional):');
    if (comment !== null) this.updateStatus(id, 'APPROVED', comment);
  }

  rejectRequest(id: number, reason: string) {
    if (!reason?.trim()) {
      alert('Introdu un motiv!');
      return;
    }
    this.updateStatus(id, 'REJECTED', reason);
    this.showRejectInput[id] = false;
  }

  loadAllRequests() {
    this.leaveService.getAllRequests().subscribe({
      next: (data) => {
        this.teamRequests = (data || []).sort((a: any, b: any) => b.id - a.id);
        this.cdr.detectChanges();
      }
    });
  }

  trackByRequestId(index: number, item: any) {
    return item.id;
  }
}