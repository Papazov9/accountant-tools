import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PricingSectionComponent } from './pricing-section.component';

describe('PricingSectionComponent', () => {
  let component: PricingSectionComponent;
  let fixture: ComponentFixture<PricingSectionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PricingSectionComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PricingSectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
