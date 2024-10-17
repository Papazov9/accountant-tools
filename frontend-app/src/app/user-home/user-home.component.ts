import {AfterViewInit, ChangeDetectorRef, Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {LoadingService} from "../services/loading.service";
import {Metrics, UserProfile, UserService} from "../services/user.service";
import {AsyncPipe, DatePipe, NgIf} from "@angular/common";
import {of, switchMap} from "rxjs";
import {Chart} from 'chart.js/auto';

@Component({
  selector: 'app-user-home',
  standalone: true,
  imports: [
    DatePipe,
    AsyncPipe,
    NgIf
  ],
  templateUrl: './user-home.component.html',
  styleUrl: './user-home.component.css'
})
export class UserHomeComponent implements OnInit, AfterViewInit {

  profile: UserProfile | null = null;
  metrics: Metrics | null = null;

  chart: any;
  mismatchData: number[] = [];

  @ViewChild('mismatchChart', {static: false}) mismatchChart!: ElementRef<HTMLCanvasElement>;


  constructor(private userService: UserService, private loadingService: LoadingService, private cdr: ChangeDetectorRef) {
  }

  ngOnInit(): void {
    this.loadingService.showOverlay();

    this.userService.fetchUserProfile().pipe(
      switchMap((userProfile) => {
        this.profile = userProfile;
        if (userProfile.username) {
          return this.userService.loadMetrics(userProfile.username);
        }

        return of(null);
      })
    ).subscribe({
      next: (metrics: Metrics | null) => {
        if (metrics && !metrics.empty) {
          this.metrics = metrics;
          this.cdr.detectChanges();
          this.initializeChart();
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

  ngAfterViewInit(): void {
    if (this.metrics) {
      this.initializeChart();
    }
  }

  updateMismatchData(metrics: Metrics | null) {
    if (!this.chart && metrics == null) {
      return;
    }
    const mismatchesTilNow = this.sumAllMismatches();
    const currentMismatchValue = metrics!.totalMismatches - mismatchesTilNow;
    this.mismatchData.push(currentMismatchValue);

    this.chart.data.labels.push(`Comparison ${this.chart.data.labels.length + 1}`);
    this.chart.update();
  }

  private sumAllMismatches(): number {
    if (this.mismatchData.length > 0) {
      return this.mismatchData.reduce((acc, value) => acc + value, 0);
    }
    return 0;
  }

  initializeChart() {
    this.chart = new Chart(this.mismatchChart.nativeElement, {
      type: 'line',
      data: {
        labels: [],
        datasets: [{
          label: `Mismatch Trends`,
          data: this.mismatchData,
          borderColor: '#483AB6',
          fill: false,
        }]
      },
      options: {
        scales: {
          x: {
            title: {
              display: true,
              text: 'Comparisons',
            }
          },
          y: {
            title: {
              display: true,
              text: 'Mismatches',
            },
            beginAtZero: true,
          }
        }
      }
    });

    this.updateMismatchData(this.metrics);
  }
}
