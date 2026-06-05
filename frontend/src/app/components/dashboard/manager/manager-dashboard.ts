import { Component, OnInit, inject, signal, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ManagerService } from '../../../services/manager.service';
import { NgxChartsModule } from '@swimlane/ngx-charts';
import { AuthService } from '../../../services/auth';
import { DragDropModule, CdkDragDrop, moveItemInArray, transferArrayItem } from '@angular/cdk/drag-drop';
import { Task, TaskService } from '../../../services/task';

@Component({
  selector: 'app-manager-dashboard',
  standalone: true,
  imports: [CommonModule, NgxChartsModule, DragDropModule],
  templateUrl: './manager-dashboard.html',
  styleUrls: ['../admin/admin-dashboard.css', './manager-dashboard.css']
})
export class ManagerDashboardComponent implements OnInit {
  private authService = inject(AuthService);
  private managerService = inject(ManagerService);
  private taskService = inject(TaskService);
  private cdr = inject(ChangeDetectorRef);
  
    // Array-uri "Master" (Sursa de adevăr pentru UI)
    todoTasks: Task[] = [];
    inProgressTasks: Task[] = [];
    doneTasks: Task[] = [];
  

  get deptId(): number {
  return this.authService.getDeptId();
}

  stats = signal<any>({ totalEmployees: 0, activeProjects: 0, pendingLeaves: 0, urgentTasks: 0 });
  recentTasks = signal<any[]>([]);
  chartData = signal<any[]>([]);
  loading = signal<boolean>(true);

  colorScheme: any = { domain: ['#6366f1', '#f59e0b', '#10b981'] };
  private readonly colorMap: Record<string, string> = {
    'TODO': '#6366f1',
    'IN_PROGRESS': '#f59e0b',
    'DONE': '#10b981'
  };

  ngOnInit() {
    this.loadMyTasks();
    this.loadManagerData();
  }

  loadMyTasks() {
    const userId = this.authService.getUserId();
    if (!userId) return;

    this.taskService.getMyTasks(userId).subscribe({
      next: (tasks) => {
        this.processTasks(tasks);
      },
      error: (err) => console.error('Error loading tasks:', err)
    });
  }

  // Separăm task-urile în array-uri locale
  processTasks(tasks: Task[]) {
    this.todoTasks = tasks.filter(t => t.status === 'TODO');
    this.inProgressTasks = tasks.filter(t => t.status === 'IN_PROGRESS');
    this.doneTasks = tasks.filter(t => t.status === 'DONE');
    this.cdr.detectChanges();
  }

  onDrop(event: CdkDragDrop<Task[]>) {
    // 1. Reordonare în aceeași coloană
    if (event.previousContainer === event.container) {
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
    } else {
      // 2. Mutare între coloane
      transferArrayItem(
        event.previousContainer.data,
        event.container.data,
        event.previousIndex,
        event.currentIndex,
      );

      // 3. Persistență la server
      const task = event.container.data[event.currentIndex];
      const newStatus = event.container.id; // ID-ul din HTML este "TODO", "IN_PROGRESS", "DONE"

      this.taskService.updateTaskStatus(task.id, newStatus).subscribe({
        error: (err) => {
          console.error('Update failed, reverting...', err);
          this.loadMyTasks(); // Refresh dacă dă eroare
        }
      });
    }
  }

  loadManagerData() {
    const currentDeptId = this.deptId;
    if (currentDeptId === 0) return;

    this.managerService.getManagerStats(currentDeptId).subscribe(data => {
        this.stats.set(data);
    });

    this.managerService.getTaskStats(currentDeptId).subscribe(data => {
        this.chartData.set(this.formatChartData(data));
        this.loading.set(false);
    });

    this.managerService.getUpcomingTasks(currentDeptId).subscribe(tasks => {
        this.recentTasks.set(tasks);
    });
}

  private formatChartData(data: any[]): any[] {
  const allStatuses = ['TODO', 'IN_PROGRESS', 'DONE'];

  const dataMap = new Map(data.map(item => [item.name || item.status, item.value || item.count || 0]));

  return allStatuses.map(status => {
    return {
      name: status,
      value: dataMap.get(status) || 0,
      color: this.colorMap[status]
    };
  });
}

  isUrgent(deadline: string | Date): boolean {
    if (!deadline) return false;
    const deadlineDate = new Date(deadline);
    const today = new Date();
    const fiveDaysFromNow = new Date();
    fiveDaysFromNow.setDate(today.getDate() + 5);

    return deadlineDate >= today && deadlineDate <= fiveDaysFromNow;
  }
}