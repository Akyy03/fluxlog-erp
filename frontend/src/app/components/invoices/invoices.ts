import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AbstractControl, FormArray, FormBuilder, FormGroup, ReactiveFormsModule, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { ProjectService, Project } from '../../services/project';
import { InvoiceService } from '../../services/invoice';

@Component({
  selector: 'app-invoices',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './invoices.html',
  styleUrl: './invoices.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class Invoices implements OnInit {
  private fb = inject(FormBuilder);
  private projectService = inject(ProjectService);
  private invoiceService = inject(InvoiceService);

  readonly minDate = new Date().toISOString().split('T')[0];

  projects = signal<Project[]>([]);
  loading = signal(false);

  totalNet = signal(0);
  totalTva = signal(0);
  totalGeneral = signal(0);

  invoiceForm!: FormGroup;

  ngOnInit() {
    this.initForm();
    this.loadProjects();
    this.setupCalculations();
  }

  get lines(): FormArray {
    return this.invoiceForm.get('lines') as FormArray;
  }

  private initForm() {
    const nextMonth = new Date();
    nextMonth.setDate(nextMonth.getDate() + 30);
    const dueDate = nextMonth.toISOString().split('T')[0];

    this.invoiceForm = this.fb.group({
      projectId: ['', Validators.required],
      invoiceNumber: ['', Validators.required],
      invoiceDate: [this.minDate, [Validators.required, this.notBeforeTodayValidator()]],
      dueDate: [dueDate, [Validators.required, this.notBeforeTodayValidator()]],
      lines: this.fb.array([this.createLineGroup()])
    });
  }

  private createLineGroup(): FormGroup {
    return this.fb.group({
      serviceName: ['', Validators.required],
      quantity: [1, [Validators.required, Validators.min(1)]],
      unitPrice: [0, [Validators.required, Validators.min(0)]]
    });
  }

  addLine() {
    this.lines.push(this.createLineGroup());
  }

  removeLine(index: number) {
    if (this.lines.length > 1) {
      this.lines.removeAt(index);
    }
  }

  private setupCalculations() {
    this.invoiceForm.valueChanges.subscribe(values => {
      const net = (values.lines as any[]).reduce((acc, line) => {
        return acc + ((line.quantity || 0) * (line.unitPrice || 0));
      }, 0);
      
      const tva = net * 0.19;
      this.totalNet.set(net);
      this.totalTva.set(tva);
      this.totalGeneral.set(net + tva);
    });
  }

  private notBeforeTodayValidator(): ValidatorFn {
    return (control: AbstractControl<string | null>): ValidationErrors | null => {
      if (!control.value) return null;
      return control.value < this.minDate ? { pastDate: true } : null;
    };
  }

  private loadProjects() {
    this.projectService.getProjects().subscribe({
      next: (data) => this.projects.set(data)
    });
  }

  onSubmit() {
    if (this.invoiceForm.invalid) {
      this.invoiceForm.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.invoiceService.generateInvoicePdf(this.invoiceForm.value).subscribe({
      next: (blob: Blob) => {
        this.downloadFile(blob, `Factura_${this.invoiceForm.value.invoiceNumber}.pdf`);
        this.loading.set(false);
      },
      error: () => {
        alert('Eroare la generarea PDF-ului.');
        this.loading.set(false);
      }
    });
  }

  private downloadFile(blob: Blob, filename: string) {
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    a.click();
    window.URL.revokeObjectURL(url);
  }
}