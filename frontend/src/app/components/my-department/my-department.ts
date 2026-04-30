import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DepartmentService } from '../../services/department.service';
import { Department } from '../../services/employee';
import { ProjectService } from '../../services/project';

@Component({
  selector: 'app-my-department',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './my-department.html',
  styleUrl: './my-department.css',
})
export class MyDepartmentComponent implements OnInit {
  department = signal<Department | null>(null);
  isEditing = signal(false);
  tempDescription = '';
  activeProjects = signal<number>(0);

  constructor(private deptService: DepartmentService, private projectService: ProjectService) {}

  ngOnInit(): void {
    this.loadDepartment();
  }

  loadDepartment(): void {
  this.deptService.getMyDepartment().subscribe({
    next: (data) => {
      this.department.set(data);
      this.tempDescription = data.description || '';
      
      if (data.id) {
        this.projectService.getActiveProjectsCount(data.id).subscribe({
          next: (count) => this.activeProjects.set(count),
          error: (err) => console.error('Eroare la numărare proiecte:', err)
        });
      }
    },
    error: (err) => console.error('Eroare la încărcarea departamentului:', err)
  });
}

  toggleEdit(): void {
    this.isEditing.update(v => !v);
    if (!this.isEditing()) {
        this.tempDescription = this.department()?.description || '';
    }
  }

  saveDescription() {
  console.log('1. Butonul a fost apăsat');
  const email = this.deptService.getEmailFromToken(); 
  console.log('2. Email extras:', email);
  
  if (!email) {
    alert("Sesiune expirată. Te rugăm să te reautentifici.");
    return;
  }

  console.log('3. Descriere de trimis:', this.tempDescription);

  this.deptService.updateDescription(email, this.tempDescription).subscribe({
    next: (updatedDept) => {
      console.log('4. Răspuns primit de la server:', updatedDept);
      this.department.set(updatedDept);
      this.isEditing.set(false);
    },
    error: (err) => {
      console.error('5. Eroare la server:', err);
      alert('Eroare: ' + (err.error?.message || 'Serverul nu a putut procesa cererea'));
    }
  });
}
}