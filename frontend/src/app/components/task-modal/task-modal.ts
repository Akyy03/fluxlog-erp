import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-task-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './task-modal.html',
  styleUrls: ['./task-modal.css'],
})
export class TaskModalComponent implements OnInit {
  @Input() projectId!: number;
  @Input() users: any[] = [];
  @Input() task: any = null;

  @Output() taskCreated = new EventEmitter<any>();
  @Output() closed = new EventEmitter<void>();

  today: string = '';

  taskModel: any = {
    id: null,
    title: '',
    description: '',
    deadline: '',
    assignedToId: null,
  };

  ngOnInit(): void {
    this.today = new Date().toISOString().split('T')[0];

    if (this.task) {
      this.taskModel = {
        id: this.task.id,
        title: this.task.title || '',
        description: this.task.description || '',
        deadline: this.task.deadline ? this.task.deadline.split('T')[0] : '',
        assignedToId: this.task.assignedTo?.id || this.task.assignedToId || null,
      };
    }
  }

  // Getter pentru filtrarea angajaților: eliminăm arhivații 
  // dar păstrăm posesorul actual al task-ului (chiar dacă e arhivat) pentru consistență la Edit
  get filteredUsers(): any[] {
    if (!this.users) return [];
    return this.users.filter(user => 
      !user.isDeleted || user.id === this.taskModel.assignedToId
    );
  }

  get isFormValid(): boolean {
    return !!(
      this.taskModel.title?.trim().length > 0 &&
      this.taskModel.deadline >= this.today &&
      this.taskModel.assignedToId
    );
  }

  submit() {
    if (this.isFormValid) {
      this.taskCreated.emit(this.taskModel);
    }
  }

  close() {
    this.closed.emit();
  }
}