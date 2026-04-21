import { Component, inject, signal } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../services/auth';
import { EmployeeService } from '../../../services/employee';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterModule, CommonModule, FormsModule],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.css',
})
export class Sidebar {
  private router = inject(Router);
  public authService = inject(AuthService);
  private employeeService = inject(EmployeeService);

  showLogoutModal = signal(false);
  showPhoneModal = signal(false);
  showPasswordModal = signal(false);

  newPhone = signal('');
  newPassword = signal('');

  // --- LOGOUT ---
  openLogoutConfirm() { this.showLogoutModal.set(true); }
  cancelLogout() { this.showLogoutModal.set(false); }
  confirmLogout() {
    this.authService.logout();
    this.showLogoutModal.set(false);
    this.router.navigate(['/login']);
  }

  // --- MODALE ---
  openPhoneModal() { this.showPhoneModal.set(true); }
  openPasswordModal() { this.showPasswordModal.set(true); }
  
  closeModals() {
    this.showPhoneModal.set(false);
    this.showPasswordModal.set(false);
    this.newPhone.set('');
    this.newPassword.set('');
  }

  // --- LOGICA DE UPDATE REALĂ ---

  savePhone() {
    const email = this.authService.getUserEmail();
    const payload = { phone: this.newPhone() };

    this.employeeService.updateMyProfile(email, payload).subscribe({
      next: (res) => {
        console.log('Phone number updated!', res);
        this.closeModals();
      },
      error: (err) => alert('Error updating phone number')
    });
  }

  savePassword() {
    const email = this.authService.getUserEmail();
    const payload = {
      user: {
        password: this.newPassword()
      }
    };

    this.employeeService.updateMyProfile(email, payload).subscribe({
      next: (res) => {
        console.log('Password changed!', res);
        this.closeModals();
        alert('Password has been changed successfully!');
      },
      error: (err) => alert('Error changing password')
    });
  }
}