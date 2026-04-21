import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TaskService } from '../../../services/task';
import { AuthService } from '../../../services/auth';

@Component({
  selector: 'app-employee-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './employee-dashboard.html',
  styleUrl: './employee-dashboard.css',
})
export class EmployeeDashboardComponent implements OnInit {
  private taskService = inject(TaskService);
  private authService = inject(AuthService);
  private cdr = inject(ChangeDetectorRef);
  todoCount = 0;
  inProgressCount = 0;
  completedCount = 0;

  activeTasks: any[] = [];

  ngOnInit() {
    const userId = this.authService.getUserId();

    if (userId) {
      this.taskService.getMyTasks(userId).subscribe((tasks) => {
        this.activeTasks = tasks
          .filter((t) => t.status !== 'DONE')
          .sort((a, b) => new Date(a.deadline).getTime() - new Date(b.deadline).getTime())
          .slice(0, 5);

        this.todoCount = tasks.filter((t) => t.status === 'TODO').length;
        this.inProgressCount = tasks.filter((t) => t.status === 'IN_PROGRESS').length;
        this.completedCount = tasks.filter((t) => t.status === 'DONE').length;

        this.cdr.detectChanges();
      });
    }
  }
}
