import { TestBed } from '@angular/core/testing';

import { InvoiceComparisonService } from './invoice-comparison.service';

describe('InvoiceComparisonService', () => {
  let service: InvoiceComparisonService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(InvoiceComparisonService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
