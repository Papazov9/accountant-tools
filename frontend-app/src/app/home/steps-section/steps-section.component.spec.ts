import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StepsSectionComponent } from './steps-section.component';

describe('StepsSectionComponent', () => {
  let component: StepsSectionComponent;
  let fixture: ComponentFixture<StepsSectionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StepsSectionComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(StepsSectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
