import {ChangeDetectorRef, Component, OnInit, ViewChild} from '@angular/core';
import {LoadingService} from "../services/loading.service";
import {Metrics, UserProfile, UserService} from "../services/user.service";
import {AsyncPipe, DatePipe, NgIf} from "@angular/common";
import {map, Observable, of, switchMap} from "rxjs";
import {BaseChartDirective} from "ng2-charts";
import {BarController, BarElement, Chart, ChartDataset, ChartOptions, ChartType} from "chart.js";
import { LinearScale, CategoryScale, LineController, PointElement, LineElement, Title, Tooltip, Legend } from 'chart.js';

@Component({
  selector: 'app-user-home',
  standalone: true,
  imports: [
    DatePipe,
    AsyncPipe,
    BaseChartDirective,
    NgIf
  ],
  templateUrl: './user-home.component.html',
  styleUrl: './user-home.component.css'
})
export class UserHomeComponent implements OnInit {

  profile: UserProfile | null = null;
  metrics: Metrics | null = null;

  mismatchData: ChartDataset[] = [
    {data: [], label: 'Mismatches'}
  ];

  mismatchLabels: string[] = [];

  chartOptions: ChartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    scales: {
      x: {
        type: 'linear',
        ticks: {
          color: '#483AB6'
        }
      },
      y: {
        beginAtZero: true,
        ticks: {
          color: '#483AB6'
        },
      },
    }
  };
  chartType: ChartType = 'line';

  @ViewChild(BaseChartDirective) chart!: BaseChartDirective;

  constructor(private userService: UserService, private loadingService: LoadingService, private cdr: ChangeDetectorRef) {
  }

  ngOnInit(): void {
    Chart.register(LinearScale, CategoryScale, LineController, PointElement, LineElement, Title, Tooltip, Legend, BarController, BarElement);
    this.loadingService.showOverlay();

    this.userService.fetchUserProfile().pipe(
      switchMap((userProfile) => {
        this.profile = userProfile;
        if (userProfile.username) {
          this.cdr.detectChanges();
          return this.userService.loadMetrics(userProfile.username);
        }

        return of(null);
      })
    ).subscribe({
      next: (metrics: Metrics | null) => {
        if (metrics && !metrics.empty) {
          this.updateMismatchData(metrics);
          this.metrics = metrics;
        }
        this.loadingService.hideOverlay();
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error loading metrics:', error);
        this.loadingService.hideOverlay();
      },
      complete: () => {
        this.loadingService.hideOverlay();
      }
    });
  }

  private updateMismatchData(metrics: Metrics | null): void {
    const comparisonCount = this.mismatchData[0].data.length + 1;
    let lastCountOfMismatches: number = 0;
    if (comparisonCount !== 0) {
      lastCountOfMismatches = this.mismatchData[0].data[comparisonCount - 1] as number;
    }
    if (metrics) {
    this.mismatchData[0].data.push(metrics?.totalMismatches - lastCountOfMismatches);
      this.mismatchLabels.push(`Comparison ${comparisonCount}`);

      if (this.chart) {
        this.chart.update();
        console.log(metrics);
        console.log(this.mismatchData[0].data);
      }
    }
  }
}
