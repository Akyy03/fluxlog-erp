import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { Employee, EmployeeService } from '../../services/employee';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-employee-detail',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './employee-detail.html',
  styleUrl: './employee-detail.css',
})
export class EmployeeDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private empService = inject(EmployeeService);

  // Folosim "role" pentru că așa este salvat în localStorage
  userRole = signal<string | null>(null);
  employee = signal<Employee | null>(null);
  showPassword = signal(false);

  ngOnInit() {
    // 1. Corecția cheii: Citim "role" în loc de "userRole"
    const rawRole = localStorage.getItem('role'); 
    if (rawRole) {
      const cleanRole = rawRole.replace(/"/g, '').trim().toUpperCase();
      this.userRole.set(cleanRole);
      console.log('Auth Check - Current User Role:', cleanRole);
    } else {
      console.warn('No role found in localStorage under key "role"');
    }

    // 2. Recuperare date angajat
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.empService.getEmployeeById(+id).subscribe({
        next: (data) => {
          this.employee.set(data);
        },
        error: (err) => {
          console.error('Error fetching employee', err);
          this.router.navigate(['/employees']);
        },
      });
    }
  }

  goBack() {
    this.router.navigate(['/employees']);
  }

  getSeniority(hireDate: string | undefined): string {
    if (!hireDate) return 'N/A';
    const start = new Date(hireDate);
    const now = new Date();
    const diffTime = Math.abs(now.getTime() - start.getTime());
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

    if (diffDays < 30) return `${diffDays} zile`;
    const months = Math.floor(diffDays / 30);
    if (months < 12) return `${months} luni`;

    const years = (diffDays / 365).toFixed(1);
    return `${years} ani in companie`;
  }

  changeRole(newRole: 'EMPLOYEE' | 'MANAGER') {
    const currentEmp = this.employee();
    if (!currentEmp || currentEmp.id === undefined) return;

    const fullName = `${currentEmp.firstName} ${currentEmp.lastName}`;
    const msg =
      newRole === 'MANAGER'
        ? `Promovati pe ${fullName} la nivelul de Management?`
        : `Revocati privilegiile de management pentru ${fullName}?`;

    if (confirm(msg)) {
      const updatedEmployee: Employee = {
        ...currentEmp,
        role: newRole,
      };

      this.empService.updateEmployee(currentEmp.id, updatedEmployee).subscribe({
        next: (res) => {
          this.employee.set(res);
          console.log('Update successful:', res);
        },
        error: (err) => {
          console.error('Update failed:', err);
          alert(`System failure: ${err.message || 'Check backend logs.'}`);
        },
      });
    }
  }

  togglePassword() {
    this.showPassword.update((v) => !v);
  }

  copyToClipboard(text: string | undefined) {
    if (text) {
      navigator.clipboard.writeText(text);
      alert('Copied!');
    }
  }
}