import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-task-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './task-modal.html',
  styleUrls: ['./task-modal.css']
})
export class TaskModalComponent implements OnInit {
  @Input() projectId!: number;
  @Input() users: any[] = [];
  @Input() task: any = null; // Prinde obiectul trimis de părinte la Edit

  @Output() taskCreated = new EventEmitter<any>();
  @Output() closed = new EventEmitter<void>();

  today: string = '';

  // Modelul local legat de input-urile din HTML prin [(ngModel)]
  taskModel: any = {
    id: null,
    title: '',
    description: '',
    deadline: '',
    assignedToId: null
  };

  ngOnInit(): void {

    this.today = new Date().toISOString().split('T')[0];

    // Dacă 'task' are valoare, înseamnă că suntem pe EDIT
    if (this.task) {
      this.taskModel = {
        id: this.task.id,
        title: this.task.title || '',
        description: this.task.description || '',
        // Curățăm data: din "2023-12-31T10:00:00" în "2023-12-31" ca să apară în calendar
        deadline: this.task.deadline ? this.task.deadline.split('T')[0] : '',
        // Luăm ID-ul de la obiectul assignedTo sau direct dacă e deja ID
        assignedToId: this.task.assignedTo?.id || this.task.assignedToId || null
      };
    }
  }

  // Verifică dacă butonul de "Finalize" poate fi apăsat
  get isFormValid(): boolean {
    return !!(
      this.taskModel.title?.trim().length > 0 && 
      this.taskModel.deadline >= this.today &&
      this.taskModel.assignedToId
    );
  }

  submit() {
    if (this.isFormValid) {
      // Trimitem taskModel către handleTaskCreated din ProjectDetails
      this.taskCreated.emit(this.taskModel);
    }
  }

  close() {
    this.closed.emit();
  }
}