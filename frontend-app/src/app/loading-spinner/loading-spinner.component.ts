import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {LoadingService} from "../services/loading.service";
import {NgIf} from "@angular/common";

@Component({
  selector: 'app-loading-spinner',
  standalone: true,
  imports: [
    NgIf
  ],
  templateUrl: './loading-spinner.component.html',
  styleUrl: './loading-spinner.component.css'
})
export class LoadingSpinnerComponent implements OnInit {

  isLoading: boolean = false;

  constructor(private loadingService: LoadingService) { }

  ngOnInit(): void {
    this.loadingService.loadingOverlay$.subscribe((loading) => {
      this.isLoading = loading;

    });
  }
}
