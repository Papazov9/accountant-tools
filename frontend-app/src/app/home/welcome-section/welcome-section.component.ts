import {Component} from '@angular/core';
import {CommonModule} from "@angular/common";

@Component({
  selector: 'app-welcome-section',
  standalone: true,
  imports: [CommonModule, CommonModule],
  templateUrl: './welcome-section.component.html',
  styleUrl: './welcome-section.component.css'
})
export class WelcomeSectionComponent {

  constructor() {
  }



}
