import { Component, Input, Output, EventEmitter, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChangeDetectorRef } from '@angular/core';

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

  selectedUser: any = null;
  today: string = '';
  private cdr = inject(ChangeDetectorRef);

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

      // Setăm obiectul selectat inițial pentru a popula dropdown-ul
      if (this.taskModel.assignedToId) {
        this.selectedUser = this.users.find(u => u.id === this.taskModel.assignedToId) || null;
      }
    }
  }

  get filteredUsers(): any[] {
    if (!this.users) return [];
    return this.users.filter(user => 
      !user.isDeleted || user.id === this.taskModel.assignedToId
    );
  }

  onUserChange(user: any) {
  this.selectedUser = user;
  this.taskModel.assignedToId = user.id;
  
  // Această linie forțează Angular să verifice din nou [disabled] de pe buton
  this.cdr.detectChanges(); 
}

  get isFormValid(): boolean {
    return !!(
      this.taskModel.title?.trim().length > 0 &&
      this.taskModel.deadline >= this.today &&
      this.taskModel.assignedToId
    );
  }

  submit() {
  if (this.selectedUser) {
    
    this.taskModel.assignedToId = this.selectedUser.userId || this.selectedUser.id + 1;
    
    this.taskCreated.emit(this.taskModel);
  }
}

  close() {
    this.closed.emit();
  }
}