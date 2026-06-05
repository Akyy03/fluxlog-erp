import { Component, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-setup-password',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './setup-password.html',
  styleUrl: './setup-password.css',
})
export class SetupPassword {
  newPassword = '';
  error = '';

  private http = inject(HttpClient);
  private router = inject(Router);

  submitNewPassword() {
    const userId = localStorage.getItem('userId');
    
    if (!userId) {
      this.error = "Eroare: Sesiune invalidă. Vă rugăm să vă autentificați din nou.";
      return;
    }

    this.http.post(`http://localhost:8080/api/users/${userId}/change-password`, this.newPassword)
      .subscribe({
        next: () => {
          localStorage.setItem('needsPasswordChange', 'false');
          this.router.navigate(['/dashboard']);
        },
        error: (err) => {
          this.error = "Eroare la actualizarea parolei.";
          console.error(err);
        }
      });
  }
}