import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ManagerService } from '../../../services/manager.service';
import { NgxChartsModule } from '@swimlane/ngx-charts';
import { AuthService } from '../../../services/auth';

@Component({
  selector: 'app-manager-dashboard',
  standalone: true,
  imports: [CommonModule, NgxChartsModule],
  templateUrl: './manager-dashboard.html',
  styleUrls: ['../admin/admin-dashboard.css']
})
export class ManagerDashboardComponent implements OnInit {
  private authService = inject(AuthService);
  private managerService = inject(ManagerService);

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
    this.loadManagerData();
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