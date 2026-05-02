import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminService, AdminStats } from '../../../services/admin.service';
import { NgxChartsModule } from '@swimlane/ngx-charts';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, NgxChartsModule],
  templateUrl: './admin-dashboard.html',
  styleUrls: ['./admin-dashboard.css'],
})
export class AdminDashboardComponent implements OnInit {
  private adminService = inject(AdminService);
  
  stats = signal<AdminStats | null>(null);
  loading = signal<boolean>(true);
  recentTasks = signal<any[]>([]);
  chartData = signal<any[]>([]);

  colorScheme: any = { domain: ['#6366f1', '#f59e0b', '#10b981'] };

  private readonly colorMap: Record<string, string> = {
    'TODO': '#6366f1',
    'IN_PROGRESS': '#f59e0b',
    'DONE': '#10b981'
  };

  ngOnInit() {
    this.adminService.getTaskStats().subscribe((data) => {
      const formattedData = data.map((item: any) => {
        const name = item.name || item.status;
        return {
          name: name,
          value: item.value || item.count || 0,
          color: this.colorMap[name] || '#64748b'
        };
      });
      this.chartData.set(formattedData);
    });

    this.adminService.getRecentTasks().subscribe((tasks) => {
      this.recentTasks.set(tasks);
    });

    this.adminService.getDashboardStats().subscribe({
      next: (data) => {
        this.stats.set(data);
        this.loading.set(false);
      }
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