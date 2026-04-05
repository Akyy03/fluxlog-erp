import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { TaskService, Task } from '../../services/task';
import { HttpClient } from '@angular/common/http';
import {
  DragDropModule,
  CdkDragDrop,
  moveItemInArray,
  transferArrayItem,
} from '@angular/cdk/drag-drop';
import { FormsModule } from '@angular/forms';
import { TaskModalComponent } from '../task-modal/task-modal';

@Component({
  selector: 'app-project-details',
  standalone: true,
  imports: [CommonModule, DragDropModule, FormsModule, TaskModalComponent],
  templateUrl: './project-details.html',
  styleUrls: ['./project-details.css'],
})
export class ProjectDetailsComponent implements OnInit {
  projectId!: number;
  tasks: Task[] = [];

  // Gestiune Departamente
  allDepartments: any[] = [];
  projectDepartmentIds: number[] = [];

  // Gestiune Editare Proiect
  isEditing = false;
  project: any = {};
  editModel: any = {};

  // Gestiune Modal Task
  showTaskModal = false;
  selectedTask: any = null;
  projectMembers: any[] = [];

  // Coloanele noastre
  todoTasks: Task[] = [];
  inProgressTasks: Task[] = [];
  doneTasks: Task[] = [];

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private taskService: TaskService,
    private http: HttpClient,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.projectId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadAllDepartments();
    // Încărcăm detaliile proiectului mai întâi pentru a avea ID-urile departamentelor
    this.loadProjectDetails();
  }

  loadTasks(): void {
    this.taskService.getTasksByProject(this.projectId).subscribe({
      next: (data) => {
        this.tasks = data;
        this.sortTasks();
        this.cdr.detectChanges();
      },
    });
  }

  loadProjectMembers(): void {
  this.http.get<any[]>('http://localhost:8080/api/employees').subscribe({
    next: (allEmployees) => {
      // 1. Ne asigurăm că avem numere pentru comparație
      const allowedDepts = this.projectDepartmentIds.map(id => Number(id));

      // 2. Filtrăm folosind noul câmp departmentId
      this.projectMembers = allEmployees.filter(emp => {
        // Dacă proiectul nu are departamente, lista rămâne goală conform logicii tale
        if (allowedDepts.length === 0) return false;

        return allowedDepts.includes(Number(emp.departmentId));
      });

      console.log('Membri eligibili găsiți:', this.projectMembers);
      this.cdr.detectChanges();
    },
    error: (err) => console.error('Error fetching employees', err)
  });
}

  loadAllDepartments(): void {
    this.http.get<any[]>('http://localhost:8080/api/departments').subscribe({
      next: (data) => {
        this.allDepartments = data;
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Error loading department names', err),
    });
  }

  loadProjectDetails(): void {
    this.http.get<any>(`http://localhost:8080/api/projects/${this.projectId}`).subscribe({
      next: (project) => {
        this.project = project;
        this.projectDepartmentIds = project.departmentIds ? Array.from(project.departmentIds) : [];
        
        // APELĂM ÎNCĂRCAREA MEMBRILOR ȘI TASK-URILOR DOAR DUPĂ CE AVEM DETALIILE PROIECTULUI
        this.loadProjectMembers();
        this.loadTasks();
        
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Error loading project details', err),
    });
  }

  // Metode Editare Proiect
  startEdit() {
    this.editModel = { ...this.project };
    this.isEditing = true;
  }

  cancelEdit() {
    this.isEditing = false;
  }

  saveProjectChanges() {
    this.http
      .put(`http://localhost:8080/api/projects/${this.projectId}`, this.editModel)
      .subscribe({
        next: (updated) => {
          this.project = updated;
          this.isEditing = false;
          this.loadProjectDetails();
        },
        error: (err) => console.error('Error updating project', err),
      });
  }

  // Metode Task Modal
  openAddTaskModal() {
    this.selectedTask = null; // Resetăm pentru task nou
    this.showTaskModal = true;
  }

  openEditTaskModal(task: any): void {
    this.selectedTask = { ...task }; // Copie pentru a evita modificarea instantanee în UI
    this.showTaskModal = true;
  }

  handleTaskCreated(taskData: any) {
    if (taskData.id) {
      // CAZUL EDITARE
      this.http.put(`http://localhost:8080/api/tasks/${taskData.id}`, taskData).subscribe({
        next: () => {
          this.loadTasks();
          this.showTaskModal = false;
        },
        error: (err) => alert('Error updating task: ' + err.message)
      });
    } else {
      // CAZUL CREARE
      const payload = { ...taskData, projectId: this.projectId, status: 'TODO' };
      this.taskService.createTask(payload).subscribe({
        next: (newTask) => {
          this.todoTasks.push(newTask);
          this.showTaskModal = false;
          this.cdr.detectChanges();
        },
        error: (err) => alert('Error creating task: ' + err.message),
      });
    }
  }

  deleteTask(taskId: number): void {
    if (confirm('Are you sure you want to terminate this task?')) {
      this.http.delete(`http://localhost:8080/api/tasks/${taskId}`).subscribe({
        next: () => {
          this.loadTasks(); 
          console.log('Task deleted successfully');
        },
        error: (err) => {
          console.error('Could not delete task', err);
          alert('Eroare la ștergere: ' + err.message);
        }
      });
    }
  }

  assignDepartment(event: any): void {
    const deptId = event.target.value;
    if (!deptId) return;

    this.http
      .post(`http://localhost:8080/api/projects/${this.projectId}/departments/${deptId}`, {})
      .subscribe({
        next: () => {
          // Reîncărcăm detaliile (care vor declanșa și loadProjectMembers)
          this.loadProjectDetails();
          event.target.value = '';
        },
        error: (err) => console.error('Error assigning department', err),
      });
  }

  removeDept(deptId: number): void {
    if (confirm('Are you sure you want to remove this department?')) {
      this.http
        .delete(`http://localhost:8080/api/projects/${this.projectId}/departments/${deptId}`)
        .subscribe({
          next: () => {
            this.loadProjectDetails();
          },
          error: (err) => console.error('Error removing department', err),
        });
    }
  }

  getDepartmentName(id: number): string {
    if (this.allDepartments.length > 0) {
      const dept = this.allDepartments.find((d) => d.id === id);
      return dept ? dept.name : `ID: ${id}`;
    }
    return '...';
  }

  getAvailableDepartments() {
    return this.allDepartments.filter((d) => !this.projectDepartmentIds.includes(d.id));
  }

  sortTasks(): void {
    this.todoTasks = this.tasks.filter((t) => t.status === 'TODO');
    this.inProgressTasks = this.tasks.filter((t) => t.status === 'IN_PROGRESS');
    this.doneTasks = this.tasks.filter((t) => t.status === 'DONE');
  }

  drop(event: CdkDragDrop<Task[]>) {
    if (event.previousContainer === event.container) {
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
    } else {
      const task = event.previousContainer.data[event.previousIndex];
      const newStatus = event.container.id as any;
      transferArrayItem(
        event.previousContainer.data,
        event.container.data,
        event.previousIndex,
        event.currentIndex,
      );
      this.taskService.updateTaskStatus(task.id, newStatus).subscribe({
        error: (err) => {
          console.error('Error updating task status', err);
          this.loadTasks();
        },
      });
    }
  }

  deleteProject() {
    if (confirm('Warning: This action will permanently delete the project and all its associated tasks. Are you sure?')) {
      this.http.delete(`http://localhost:8080/api/projects/${this.projectId}`).subscribe({
        next: () => {
          this.router.navigate(['/projects']);
        },
        error: (err) => alert('Error deleting project: ' + err.message),
      });
    }
  }
}