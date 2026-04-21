import { Component, OnInit, ChangeDetectorRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DragDropModule, CdkDragDrop, moveItemInArray, transferArrayItem } from '@angular/cdk/drag-drop';
import { Task, TaskService } from '../../services/task';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-my-tasks',
  standalone: true,
  imports: [CommonModule, DragDropModule],
  templateUrl: './my-tasks.html',
  styleUrl: './my-tasks.css'
})
export class MyTasksComponent implements OnInit {
  private taskService = inject(TaskService);
  private authService = inject(AuthService);
  private cdr = inject(ChangeDetectorRef);

  // Array-uri "Master" (Sursa de adevăr pentru UI)
  todoTasks: Task[] = [];
  inProgressTasks: Task[] = [];
  doneTasks: Task[] = [];

  ngOnInit() {
    this.loadMyTasks();
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
}