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

  currentUserId = Number(localStorage.getItem('userId'));
  deptId = Number(localStorage.getItem('deptId'));

  isAdmin = this.authService.isAdmin();
  isManager = this.authService.isManager();

  newRequest = { startDate: '', endDate: '', type: 'VACATION', reason: '' };

  // PROPRIETĂȚI NOI PENTRU CALCUL
  remainingDays: number = 0;
  selectedWorkDays: number = 0;

  myRequests: any[] = [];
  teamRequests: any[] = [];
  showRejectInput: { [key: number]: boolean } = {};

  message: string = '';
  isSubmitting = false;
  minDate: string = '';

  ngOnInit(): void {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    this.minDate = tomorrow.toISOString().split('T')[0];

    this.loadData();
  }

  // LOGICĂ NOUĂ: Calcul zile lucrătoare (exclude weekend)
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
      const dayOfWeek = cur.getDay(); // 0 = Duminică, 6 = Sâmbătă
      if (dayOfWeek !== 0 && dayOfWeek !== 6) {
        count++;
      }
      cur.setDate(cur.getDate() + 1);
    }
    this.selectedWorkDays = count;
  }

  loadData() {
    if (this.isAdmin) {
      this.loadAllRequests();
    } else {
      if (this.currentUserId) {
        this.loadEmployeeRequests();
      }
      if (this.isManager && this.deptId) {
        this.loadDepartmentRequests();
      }
    }
  }

  loadEmployeeRequests() {
    this.leaveService.getMyRequests(this.currentUserId).subscribe({
      next: (data) => {
        this.myRequests = [...data];
        
        // Extragem balanța din obiectul employee al primei cereri gasite
        if (data && data.length > 0 && data[0].employee) {
          this.remainingDays = data[0].employee.remainingLeaveDays;
        }
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error(err);
        this.message = 'Error loading personal history.';
      },
    });
  }

  loadDepartmentRequests() {
    this.leaveService.getDepartmentRequests(this.deptId).subscribe({
      next: (data) => {
        const all = data || [];
        this.teamRequests = all.filter(req => 
          req.employee?.position !== 'Architect' && 
          req.employee?.id !== this.currentUserId
        );
        this.cdr.detectChanges();
      }
    });
  }

  loadAllRequests() {
    this.leaveService.getAllRequests().subscribe({
      next: (data) => {
        this.teamRequests = (data || []).filter(
          (req: any) => req.employee.id !== this.currentUserId
        );
        this.cdr.detectChanges();
      },
      error: (err) => (this.message = 'Error loading global registry.'),
    });
  }

  submitRequest() {
    if (!this.newRequest.startDate || !this.newRequest.endDate) {
      this.message = 'Please select the period.';
      return;
    }

    this.isSubmitting = true;
    this.leaveService.createRequest(this.currentUserId, this.newRequest).subscribe({
      next: (res) => {
        this.isSubmitting = false;
        this.message = 'Success! Request submitted.';
        this.resetForm();
        this.loadEmployeeRequests();
      },
      error: (err) => {
        this.isSubmitting = false;
        this.message = 'Eroare: ' + (err.error?.message || 'Nu s-a putut trimite cererea.');
      },
    });
  }

  private resetForm() {
    this.newRequest = { startDate: '', endDate: '', type: 'VACATION', reason: '' };
    this.selectedWorkDays = 0; // Resetam si calculul vizual
  }

  deleteRequest(id: number) {
    if (confirm('Are you sure you want to delete this request?')) {
      this.leaveService.deleteRequest(id).subscribe({
        next: () => {
          this.message = 'Request deleted successfully.';
          this.myRequests = this.myRequests.filter((req) => req.id !== id);
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('Error deleting request:', err);
          this.message = 'Error deleting request. Please try again.';
        },
      });
    }
  }

  toggleRejectBox(id: number) {
    this.showRejectInput[id] = !this.showRejectInput[id];
  }

  approveRequest(id: number) {
    const comment = prompt('Optional: Observations for approval:');
    if (comment !== null) {
      this.updateStatus(id, 'APPROVED', comment);
    }
  }

  rejectRequest(id: number, reason: string) {
    if (!reason || reason.trim() === '') {
      alert('Te rugăm să introduci un motiv pentru respingere.');
      return;
    }
    this.updateStatus(id, 'REJECTED', reason);
    this.showRejectInput[id] = false;
  }

  private updateStatus(id: number, status: 'APPROVED' | 'REJECTED', comment: string = '') {
    this.isSubmitting = true;
    this.leaveService.updateRequestStatus(id, status, comment).subscribe({
      next: () => {
        this.message = `Request successfully ${status.toLowerCase()}.`;
        this.isSubmitting = false;
        this.loadData(); 
      },
      error: (err) => {
        this.message = 'Error: ' + (err.error?.message || 'Failed to update status.');
        this.isSubmitting = false;
        this.cdr.detectChanges();
      }
    });
  }

  trackByRequestId(index: number, item: any) {
    return item.id;
  }
}