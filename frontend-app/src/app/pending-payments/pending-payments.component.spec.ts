import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PendingPaymentsComponent } from './pending-payments.component';

describe('PendingPaymentsComponent', () => {
  let component: PendingPaymentsComponent;
  let fixture: ComponentFixture<PendingPaymentsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PendingPaymentsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PendingPaymentsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
