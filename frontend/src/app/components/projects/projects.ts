import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Project, ProjectService } from '../../services/project';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-projects',
  templateUrl: './projects.html',
  styleUrls: ['./projects.css'],
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
})
export class ProjectsComponent implements OnInit {
  projects: Project[] = [];
  loading: boolean = true;
  showModal: boolean = false; // Controlul vizibilității modalului
  projectForm: FormGroup;

  today: string = '';

  constructor(
    private projectService: ProjectService,
    private cdr: ChangeDetectorRef,
    private fb: FormBuilder,
  ) {
    // Inițializarea formularului cu validări de bază
    this.projectForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3)]],
      description: ['', Validators.required],
      deadline: ['', Validators.required],
      status: ['ACTIVE', Validators.required],
    });
  }

  ngOnInit(): void {
    this.today = new Date().toISOString().split('T')[0];
    this.loadProjects();
  }

  loadProjects(): void {
    this.projectService.getProjects().subscribe({
      next: (data) => {
        this.projects = data;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error loading projects', err);
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }

  // Metode pentru controlul modalului
  openModal(): void {
    this.showModal = true;
    this.cdr.detectChanges();
  }

  closeModal(): void {
    this.showModal = false;
    this.projectForm.reset({ status: 'ACTIVE' }); // Resetăm dar păstrăm statusul default
    this.cdr.detectChanges();
  }

  // Trimiterea formularului către Backend
  submitProject(): void {
    if (this.projectForm.valid) {
      this.projectService.createProject(this.projectForm.value).subscribe({
        next: (newProject) => {
          // Adăugăm proiectul nou primit de la server direct în listă
          this.projects.push(newProject);
          this.closeModal();
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('Error saving project', err);
          alert('A apărut o eroare la salvarea proiectului!');
        },
      });
    }
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'ACTIVE':
        return 'status-active';
      case 'ON_HOLD':
        return 'status-hold';
      case 'COMPLETED':
        return 'status-completed';
      default:
        return '';
    }
  }
}
