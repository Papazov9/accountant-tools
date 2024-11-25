import { Component } from '@angular/core';
import {AppSelectionService} from "../services/app-selection.service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-app-selection-overlay',
  standalone: true,
  imports: [],
  templateUrl: './app-selection-overlay.component.html',
  styleUrl: './app-selection-overlay.component.css'
})
export class AppSelectionOverlayComponent {


  constructor(private appSelectionService: AppSelectionService, private router: Router) {
  }

  selectApp(app: 'invoice-comparison' | 'inventory') {
    this.appSelectionService.setSelectedApp(app);
    this.router.navigate([`/${app}/dashboard`]);
  }
}
