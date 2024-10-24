import {AfterViewInit, ChangeDetectorRef, Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {LoadingService} from "../services/loading.service";
import {ComparisonMetric, Metrics, UserProfile, UserService} from "../services/user.service";
import {AsyncPipe, DatePipe, NgIf} from "@angular/common";
import {of, switchMap} from "rxjs";
import {Chart} from 'chart.js/auto';
import {ToggleService} from "../services/toggle.service";

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
export class UserHomeComponent implements OnInit {

  profile: UserProfile | null = null;
  metrics: Metrics | null = null;

  chart: any;
  pieChart: any;
  mismatchData: number[] = [];

  @ViewChild('mismatchChart', {static: false}) mismatchChart?: ElementRef<HTMLCanvasElement>;
  @ViewChild('comparisonPieChart', {static: false}) comparisonPieChart?: ElementRef<HTMLCanvasElement>;


  constructor(private userService: UserService, private loadingService: LoadingService, private cdr: ChangeDetectorRef, private toggleService: ToggleService) {
  }

  ngOnInit(): void {
    this.loadingService.showOverlay();

    this.userService.fetchUserProfile().pipe(
      switchMap((userProfile) => {
        if (userProfile) {
          this.profile = userProfile;
          if (!userProfile.isAcknowledge) {
            this.toggleService.showSubscriptions();
          }
          if (userProfile.username) {
            return this.userService.loadMetrics(userProfile.username);
          }
        }
        return of(null);
      })
    ).subscribe({
      next: (metrics: Metrics | null) => {
        if (metrics && !metrics.empty) {
          this.metrics = metrics;
          this.cdr.detectChanges();
          this.initializeCharts();
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

  updateMismatchData(metrics: Metrics | null) {
    if (!this.chart && metrics == null) {
      return;
    }

    console.log(metrics?.comparisonMetricResponses);
    this.mismatchData = [];
    this.chart.data.labels = [];

    metrics!.comparisonMetricResponses.forEach((comparisonMetric: ComparisonMetric, index: number) => {
      this.mismatchData.push(comparisonMetric.mismatches);
      this.chart.data.labels.push(`Comparison ${index + 1}`)
    })

    this.chart.data.datasets[0].data = this.mismatchData;
    this.chart.update();
  }

  private initializeCharts() {
    this.initializeChart();
    this.initializePieChart()
  }

  initializePieChart() {
    if (this.comparisonPieChart && this.comparisonPieChart.nativeElement) {
      this.chart = new Chart(this.comparisonPieChart.nativeElement, {
        type: 'pie',
        data: {
          labels: ['Successful Comparisons', 'Failed Comparisons'],
          datasets: [{
            label: 'Comparison Success Rate',
            data: [this.metrics?.successfulComparisons, this.metrics?.failedComparisons],
            backgroundColor: ['#483AB6', '#D3D3D3'], // Purple for successful, Gray for failed
            hoverBackgroundColor: ['#6B59D3', '#A9A9A9'], // Slightly lighter on hover
            borderColor: '#FFFFFF',
            hoverBorderColor: '#FFFFFF',
            borderWidth: 2,
            hoverBorderWidth: 3,
            hoverOffset: 10
          }]
        },
        options: {
          layout: {
            padding: 15
          },
          responsive: true,
          plugins: {
            legend: {
              position: 'bottom',
              align: 'start',
              labels: {
                color: '#5C5CFF',
                font: {
                  size: 14,
                  weight: 'bold',
                  family: 'Arial'
                }
              }
            },
            tooltip: {
              backgroundColor: '#483AB6',
              titleColor: '#fff',
              bodyColor: '#fff',
              borderColor: '#fff',
              borderWidth: 1,
            }
          },
          animation: {
            animateRotate: true,
            animateScale: true,
          },
          scales: {
          }
        }
      });
    }
  }

  initializeChart() {
    if (this.mismatchChart && this.mismatchChart.nativeElement) {

      // @ts-ignore
      const gradient = this.mismatchChart.nativeElement.getContext('2d').createLinearGradient(0, 0, 0, 400);
      gradient.addColorStop(0, 'rgba(72, 58, 182, 0.5)');
      gradient.addColorStop(1, 'rgba(72, 58, 182, 0)');

      this.chart = new Chart(this.mismatchChart.nativeElement, {
        type: 'line',
        data: {
          labels: [],
          datasets: [{
            label: 'Mismatch Trends',
            data: this.mismatchData,
            borderColor: '#483AB6',
            backgroundColor: gradient,
            borderWidth: 2,
            pointBorderColor: '#fff',
            pointBackgroundColor: '#483AB6',
            pointHoverBorderColor: '#fff',
            pointHoverBackgroundColor: '#483AB6',
            pointRadius: 5,
            pointHoverRadius: 7,
            tension: 0.4, // Creates smooth curves instead of sharp lines
            fill: true, // Fill under the line with the gradient
            // shadowOffsetX: 3, // Adds a shadow to the line for depth
            // shadowOffsetY: 3,
            // shadowBlur: 5,
            // shadowColor: 'rgba(0, 0, 0, 0.3)'
          }]
        },
        options: {
          responsive: true,
          scales: {
            x: {
              title: {
                display: true,
                text: 'Comparisons',
                color: '#5C5CFF',
                font: {
                  size: 14,
                  weight: 'bold',
                  family: 'Arial'
                },
              },
              grid: {
                color: 'rgba(255, 255, 255, 0.1)', // Light gridlines
              },
              ticks: {
                color: '#5C5CFF', // White text on x-axis
              },
            },
            y: {
              title: {
                display: true,
                text: 'Mismatches Count',
                color: '#5C5CFF',
                font: {
                  size: 14,
                  weight: 'bold',
                  family: 'Arial'
                },
              },
              grid: {
                display: false,
              },
              ticks: {
                color: '#5C5CFF', // White text on y-axis
              },
              beginAtZero: true,
              border: {
                display: false
              }
            }
          },
          plugins: {
            tooltip: {
              backgroundColor: '#483AB6',
              titleColor: '#5C5CFF',
              bodyColor: '#fff',
              borderColor: '#fff',
              borderWidth: 1,
            },
            legend: {
              labels: {
                color: '#5C5CFF', // White legend text
              }
            }
          },
          elements: {
            line: {
              borderWidth: 3
            },
            point: {
              radius: 6
            }
          }
        }
      });

      this.updateMismatchData(this.metrics);

    }
  }
}
