import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InvoiceComparisonComponent } from './invoice-comparison.component';

describe('InvoiceComparisonComponent', () => {
  let component: InvoiceComparisonComponent;
  let fixture: ComponentFixture<InvoiceComparisonComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InvoiceComparisonComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InvoiceComparisonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
