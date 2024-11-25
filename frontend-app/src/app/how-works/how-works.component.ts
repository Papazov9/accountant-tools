import {AfterViewInit, Component, OnDestroy, OnInit} from '@angular/core';
import {NgForOf} from "@angular/common";
import {RouterLink} from "@angular/router";

export interface Step {
  id: number,
  title: string,
  description: string,
  imageUrl: string,
}

@Component({
  selector: 'app-how-works',
  standalone: true,
  imports: [
    NgForOf,
    RouterLink
  ],
  templateUrl: './how-works.component.html',
  styleUrl: './how-works.component.css'
})
export class HowWorksComponent implements OnInit, AfterViewInit, OnDestroy{
  steps: Step[] = [
    { id: 1, title: $localize`Go to Invoice Comparison Tab`, description: $localize`Use side navigation bar after successful login and click the Invoice Comparison Tab.`, imageUrl: '/assets/images/step1.gif' },
    { id: 2, title: $localize`Upload Your Files`, description: $localize`Upload your .csv and .txt files in the appropriate upload fields. The number of uploaded files in both fields has to be equal!`, imageUrl: '/assets/images/step2.gif' },
    { id: 3, title: $localize`Automatic Comparison`, description: $localize`Our system matches records by key data points like document number, date, and amount. No more manual Excel matching, we\'ve got you covered with UNPARALLELED speed of execution.`, imageUrl: '/assets/images/step3.gif' },
    { id: 4, title: $localize`Get Results Instantly`, description: $localize`Receive a detailed Excel report with separate sheets for each type of mismatch. Review audit-ready results for complete accuracy.`, imageUrl: '/assets/images/step4.gif' },
  ];
  currentStep: Step = this.steps[0];
  observer: IntersectionObserver | null = null;

  ngOnInit() {
  }

  ngAfterViewInit() {
    const options = {
      root: null,
      threshold: 0.85
    };

    this.observer = new IntersectionObserver(this.handleIntersection.bind(this), options);

    // Observe each step content element
    this.steps.forEach(step => {
      const element = document.getElementById(`step-${step.id}`);
      if (element && this.observer) {
        this.observer.observe(element);
      }
    });

    setTimeout(() => {
      this.checkInitialVisibleStep();
    }, 100);
  }

  handleIntersection(entries: IntersectionObserverEntry[]) {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        const stepId = parseInt(entry.target.id.split('-')[1], 10);
        const step = this.steps.find(s => s.id === stepId);
        if (step) {
          this.currentStep = step;
        }
      }
    });
  }

  checkInitialVisibleStep() {
    const firstVisibleStep = this.steps.find(step => {
      const element = document.getElementById(`step-${step.id}`);
      return element && element.getBoundingClientRect().top < window.innerHeight / 2;
    });

    if (firstVisibleStep) {
      this.currentStep = firstVisibleStep;
    } else {
      this.currentStep = this.steps[0];
    }
  }

  selectStep(step: Step) {
    this.currentStep = step;
    const element = document.getElementById(`step-${step.id}`);
    if (element) {
      element.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }
  }

  ngOnDestroy() {
    if (this.observer) {
      this.observer.disconnect();
    }
  }

}
