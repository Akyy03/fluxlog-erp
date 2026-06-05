import { Component, OnInit, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EmployeeService, Department } from '../../services/employee';
import { FormsModule } from '@angular/forms';
import { DepartmentService } from '../../services/department.service';

@Component({
  selector: 'app-department-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './department-list.html',
  styleUrl: './department-list.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DepartmentList implements OnInit {
  private deptService = inject(DepartmentService);
  private empService = inject(EmployeeService);

  departments = signal<Department[]>([]);
  isModalOpen = signal(false);
  isEditMode = signal(false);
  currentDept = signal<Department>({ name: '', description: '' });

  loadDepartments() {
    this.deptService.getDepartments().subscribe((data) => this.departments.set(data));
  }

  ngOnInit() {
    this.loadDepartments();
  }

  // Verificări de unicitate
  isNameDuplicate(): boolean {
    const name = this.currentDept().name?.toLowerCase().trim();
    if (!name || name.length < 3) return false;
    return this.departments().some(d => 
      d.id !== this.currentDept().id && 
      d.name.toLowerCase().trim() === name
    );
  }

  isDescDuplicate(): boolean {
    const desc = this.currentDept().description?.toLowerCase().trim();
    if (!desc || desc.length < 10) return false;
    return this.departments().some(d => 
      d.id !== this.currentDept().id && 
      d.description?.toLowerCase().trim() === desc
    );
  }

  onDelete(id: number | undefined) {
    if (id === undefined) return;
    if (confirm('Sunteti sigur? Aceasta actiune va sterge unitatea si va afecta angajatii alocati.')) {
      this.deptService.deleteDepartment(id).subscribe({
        next: () => this.loadDepartments(),
        error: (err) => {
          if (err.status === 500) {
            alert(`ACTION DENIED: Cannot decommission department.\n\nThis department is still linked to active employees.`);
          } else {
            alert('System Error: Could not complete the deletion request.');
          }
        },
      });
    }
  }

  openAddModal() {
    this.isEditMode.set(false);
    this.currentDept.set({ name: '', description: '' });
    this.isModalOpen.set(true);
  }

  openEditModal(dept: Department) {
    this.isEditMode.set(true);
    this.currentDept.set({ ...dept });
    this.isModalOpen.set(true);
  }

  closeModal() {
    this.isModalOpen.set(false);
  }

  saveDepartment() {
    const deptData = this.currentDept();
    
    if (!deptData.name || deptData.name.trim().length < 3) return;
    if (!deptData.description || deptData.description.trim().length < 10) return;
    if (this.isNameDuplicate() || this.isDescDuplicate()) {
      alert("CRITICAL ERROR: Duplicate Identity Detected.");
      return;
    }

    if (this.isEditMode()) {
      this.deptService.updateDepartment(deptData.id!, deptData).subscribe(() => {
        this.loadDepartments();
        this.closeModal();
      });
    } else {
      this.deptService.addDepartment(deptData).subscribe(() => {
        this.loadDepartments();
        this.closeModal();
      });
    }
  }
}