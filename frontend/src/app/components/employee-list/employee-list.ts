import {
  Component,
  OnInit,
  inject,
  signal,
  computed,
  ChangeDetectionStrategy,
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { EmployeeService, Employee, Department } from '../../services/employee';
import { Router } from '@angular/router';

@Component({
  selector: 'app-employee-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './employee-list.html',
  styleUrl: './employee-list.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EmployeeListComponent implements OnInit {
  viewMode = signal<'grid' | 'table'>('grid');
  private router = inject(Router);
  private employeeService = inject(EmployeeService);

  // --- STATE MANAGEMENT ---
  employees = signal<Employee[]>([]);
  departments = signal<Department[]>([]);
  isModalOpen = signal(false);
  errorMessage = signal<string | null>(null);
  userRole = signal<string | null>(null);

  isEditMode = signal(false);
  editingEmployeeId = signal<number | null>(null);

  searchQuery = signal('');
  showDeleted = signal(false);

  newEmployee: any = {
    firstName: '',
    lastName: '',
    position: '',
    salary: 0,
    email: '',
    phone: '',
    hireDate: new Date().toISOString().split('T')[0],
    department: { id: undefined, name: '' },
  };

  // --- CALCULATED DATA ---
  totalEmployees = computed(() => this.employees().filter((e) => !e.isDeleted).length);

  totalBudget = computed(() => {
    const role = this.userRole();
    const emps = this.filteredEmployees();

    if (role === 'ADMIN') {
      return emps.reduce((acc, emp) => acc + (emp.salary || 0), 0);
    } else {
      return emps
        .filter((emp) => emp.role !== 'MANAGER')
        .reduce((acc, emp) => acc + (emp.salary || 0), 0);
    }
  });

  avgSalary = computed(() =>
    this.totalEmployees() > 0 ? this.totalBudget() / this.totalEmployees() : 0,
  );

  // --- LOGICA DE FILTRARE CORECTATĂ (SEARCH + ARCHIVE) ---
  filteredEmployees = computed(() => {
    const allEmployees = this.employees();
    const displayArchive = this.showDeleted();
    const isAdmin = this.userRole()?.includes('ADMIN');
    const query = this.searchQuery().toLowerCase().trim();

    return allEmployees.filter((emp: Employee) => {
      // 1. Filtru de Arhivă (Logica existentă)
      const archived = !!emp.isDeleted;
      let matchesStatus = false;

      if (isAdmin) {
        matchesStatus = (displayArchive === archived);
      } else {
        matchesStatus = !archived;
      }

      // Dacă nu se potrivește statusul (Active/Archive), tăiem din start
      if (!matchesStatus) return false;

      // 2. Filtru de Search (Logica adăugată)
      if (!query) return true; // Dacă nu scriem nimic, arătăm tot din categoria respectivă

      const fullName = `${emp.firstName} ${emp.lastName}`.toLowerCase();
      const position = (emp.position || '').toLowerCase();
      const dept = (emp.departmentName || '').toLowerCase();

      return fullName.includes(query) || position.includes(query) || dept.includes(query);
    });
  });

  ngOnInit(): void {
    this.userRole.set(localStorage.getItem('role'));
    this.loadEmployees();
    this.loadDepartments();
  }

  // --- ACTIONS ---
  onSearch(event: Event) {
    const input = event.target as HTMLInputElement;
    this.searchQuery.set(input.value);
  }

  loadEmployees() {
    this.employeeService.getEmployees().subscribe({
      next: (data: any) => {
        const employeesArray = Array.isArray(data) ? data : (data.content || []);
        
        let normalized: Employee[] = employeesArray.map((emp: any) => {
          const hasDeletedFlag = emp.isDeleted === true || emp.deleted === true;
          const hasActiveFlag = emp.isActive === false || emp.active === false;

          return {
            ...emp,
            isDeleted: hasDeletedFlag || hasActiveFlag
          };
        });

        const role = this.userRole();
        const myEmail = localStorage.getItem('email');

        if (role?.includes('MANAGER') && !role?.includes('ADMIN') && myEmail) {
          const myProfile = normalized.find((e: Employee) => e.email === myEmail);
          if (myProfile?.departmentName) {
            this.employees.set(normalized.filter((e: Employee) => e.departmentName === myProfile.departmentName));
          } else {
            this.employees.set(normalized);
          }
        } else {
          this.employees.set(normalized);
        }
      },
      error: (err) => console.error('API Error:', err),
    });
  }

  loadDepartments() {
    this.employeeService.getDepartments().subscribe({
      next: (data) => this.departments.set(data),
      error: (err) => console.error('Error loading departments:', err),
    });
  }

  // --- MODAL MANAGEMENT ---
  openAddModal() {
    this.isEditMode.set(false);
    this.editingEmployeeId.set(null);
    this.resetForm();
    this.isModalOpen.set(true);
  }

  openEditModal(employee: any) {
    this.isEditMode.set(true);
    this.editingEmployeeId.set(employee.id || null);

    const foundDept = this.departments().find((d) => d.name === employee.departmentName);

    this.newEmployee = {
      ...employee,
      hireDate: employee.hireDate
        ? new Date(employee.hireDate).toISOString().split('T')[0]
        : new Date().toISOString().split('T')[0],
      department: foundDept ? { ...foundDept } : { id: undefined, name: '' },
    };

    this.isModalOpen.set(true);
  }

  closeModal() {
    this.isModalOpen.set(false);
    this.errorMessage.set(null);
    this.resetForm();
  }

  resetForm() {
    this.newEmployee = {
      firstName: '',
      lastName: '',
      position: '',
      salary: 0,
      email: '',
      phone: '',
      hireDate: new Date().toISOString().split('T')[0],
      department: { id: undefined, name: '' },
    };
  }

  validatePhone(event: any) {
    const input = event.target;
    const cleanedValue = input.value.replace(/[^0-9+]/g, '');
    this.newEmployee.phone = cleanedValue;
    input.value = cleanedValue;
  }

  // --- CRUD OPERATIONS ---
  saveEmployee() {
    this.errorMessage.set(null);

    const employeeData: Employee = {
      ...this.newEmployee,
      id: this.editingEmployeeId() || undefined,
      isDeleted: this.isEditMode() ? !!this.newEmployee.isDeleted : false,
    };

    const request = (this.isEditMode() && this.editingEmployeeId())
      ? this.employeeService.updateEmployee(this.editingEmployeeId()!, employeeData)
      : this.employeeService.addEmployee(employeeData);

    request.subscribe({
      next: () => {
        this.loadEmployees();
        this.closeModal();
      },
      error: (err) => this.handleBackendError(err),
    });
  }

  deleteEmployee(id: number) {
    if (confirm('Are you sure you want to archive this employee?')) {
      this.employeeService.deleteEmployee(id).subscribe({
        next: () => this.loadEmployees(),
        error: (err) => console.error('Error archiving employee:', err),
      });
    }
  }

  restoreEmployee(id: number) {
    if (confirm('Reactivate this employee and their user account?')) {
      this.employeeService.restoreEmployee(id).subscribe({
        next: () => {
          this.showDeleted.set(false);
          this.loadEmployees();
        },
        error: (err) => console.error('Restore failed:', err),
      });
    }
  }

  private handleBackendError(err: any) {
    this.errorMessage.set(
      err.status === 409 ? 'Identity Conflict: Email or Phone already exists.' : 'System Error: Operation failed.'
    );
  }

  viewEmployeeProfile(id: number | undefined) {
    if (id) this.router.navigate(['/employees', id]);
  }
}