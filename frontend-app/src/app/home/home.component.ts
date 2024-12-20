import {Component, Renderer2, OnInit} from '@angular/core';
import {StepsSectionComponent} from "./steps-section/steps-section.component";
import {OverviewSectionComponent} from "./overview-section/overview-section.component";
import {WelcomeSectionComponent} from "./welcome-section/welcome-section.component";
import {CommonModule} from "@angular/common";
import {PricingSectionComponent} from "./pricing-section/pricing-section.component";

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css'],
  standalone: true,
  imports: [
    CommonModule,
    WelcomeSectionComponent,
    OverviewSectionComponent,
    StepsSectionComponent,
    PricingSectionComponent
  ]
})
export class HomeComponent {

  constructor() {}


}
