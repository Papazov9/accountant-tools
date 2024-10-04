import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OverviewSectionComponent } from './overview-section.component';

describe('OverviewSectionComponent', () => {
  let component: OverviewSectionComponent;
  let fixture: ComponentFixture<OverviewSectionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OverviewSectionComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OverviewSectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
