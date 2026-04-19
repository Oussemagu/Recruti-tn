import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardService } from '../../../core/services/dashboard.service';
import { Chart, registerables, ChartConfiguration } from 'chart.js';

Chart.register(...registerables);

interface RoleStats {
  CANDIDAT: number;
  RECRUTEUR: number;
  ADMIN: number;
}

interface SkillStats {
  [skill: string]: number;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit, OnDestroy {
  private dashboardService = inject(DashboardService);

  roleChart: Chart | null = null;
  skillChart: Chart | null = null;

  // Stats
  totalUsers = 0;
  candidateCount = 0;
  recruiterCount = 0;
  uniqueSkillsCount = 0;

  ngOnInit(): void {
    this.loadRoleStatistics();
    this.loadSkillStatistics();
  }

  loadRoleStatistics(): void {
    this.dashboardService.getRoleStatistics().subscribe({
      next: (data: RoleStats) => {
        this.candidateCount = data.CANDIDAT;
        this.recruiterCount = data.RECRUTEUR;
        this.totalUsers = data.CANDIDAT + data.RECRUTEUR + data.ADMIN;
        this.createRoleChart(data);
      },
      error: (err: any) => console.error('Error loading role stats:', err)
    });
  }

  loadSkillStatistics(): void {
    this.dashboardService.getSkillStatistics().subscribe({
      next: (data: SkillStats) => {
        this.uniqueSkillsCount = Object.keys(data).length;
        this.createSkillChart(data);
      },
      error: (err: any) => console.error('Error loading skill stats:', err)
    });
  }

  createRoleChart(data: RoleStats): void {
    const ctx = document.getElementById('roleChart') as HTMLCanvasElement;

    if (this.roleChart) {
      this.roleChart.destroy();
    }

    const config: ChartConfiguration<'pie'> = {
      type: 'pie',
      data: {
        labels: ['Candidates', 'Recruiters', 'Admins'],
        datasets: [{
          data: [data.CANDIDAT, data.RECRUTEUR, data.ADMIN],
          backgroundColor: ['#1e40af', '#3b82f6', '#93c5fd'],
          borderWidth: 3,
          borderColor: '#ffffff',
          hoverOffset: 8
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'bottom',
            labels: {
              padding: 20,
              font: {
                size: 13,
                weight: 600,
                family: 'inherit'
              },
              color: '#374151',
              usePointStyle: true,
              pointStyle: 'circle'
            }
          },
          tooltip: {
            backgroundColor: '#1f2937',
            padding: 12,
            cornerRadius: 8,
            titleFont: {
              size: 14,
              weight: 'bold'
            },
            bodyFont: {
              size: 13
            },
            callbacks: {
              label: (context: import('chart.js').TooltipItem<'pie'>) => {
                const label = context.label || '';
                const value = context.parsed || 0;
                const total = (context.dataset.data as number[]).reduce((a, b) => a + b, 0);
                const percentage = ((value / total) * 100).toFixed(1);
                return ` ${label}: ${value} (${percentage}%)`;
              }
            }
          }
        }
      }
    };

    this.roleChart = new Chart(ctx, config);
  }

  createSkillChart(data: SkillStats): void {
    const ctx = document.getElementById('skillChart') as HTMLCanvasElement;

    if (this.skillChart) {
      this.skillChart.destroy();
    }

    const labels = Object.keys(data);
    const values = Object.values(data);
    const colors = [
      '#1e3a8a', '#1e40af', '#1d4ed8', '#2563eb', '#3b82f6',
      '#60a5fa', '#93c5fd', '#bfdbfe', '#0369a1', '#0284c7'
    ];

    const config: ChartConfiguration<'pie'> = {
      type: 'pie',
      data: {
        labels: labels.map(skill => this.capitalizeSkill(skill)),
        datasets: [{
          data: values,
          backgroundColor: colors.slice(0, labels.length),
          borderWidth: 3,
          borderColor: '#ffffff',
          hoverOffset: 8
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'bottom',
            labels: {
              padding: 20,
              font: {
                size: 12,
                weight: 600,
                family: 'inherit'
              },
              color: '#374151',
              usePointStyle: true,
              pointStyle: 'circle'
            }
          },
          tooltip: {
            backgroundColor: '#1f2937',
            padding: 12,
            cornerRadius: 8,
            titleFont: {
              size: 14,
              weight: 'bold'
            },
            bodyFont: {
              size: 13
            },
            callbacks: {
              label: (context: import('chart.js').TooltipItem<'pie'>) => {
                const label = context.label || '';
                const value = context.parsed || 0;
                const total = (context.dataset.data as number[]).reduce((a, b) => a + b, 0);
                const percentage = ((value / total) * 100).toFixed(1);
                return ` ${label}: ${value} candidates (${percentage}%)`;
              }
            }
          }
        }
      }
    };

    this.skillChart = new Chart(ctx, config);
  }

  capitalizeSkill(skill: string): string {
    return skill.split(' ')
      .map(word => word.charAt(0).toUpperCase() + word.slice(1))
      .join(' ');
  }

  ngOnDestroy(): void {
    if (this.roleChart) {
      this.roleChart.destroy();
    }
    if (this.skillChart) {
      this.skillChart.destroy();
    }
  }
}
