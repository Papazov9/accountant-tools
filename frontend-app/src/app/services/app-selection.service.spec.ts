import { TestBed } from '@angular/core/testing';

import { AppSelectionService } from './app-selection.service';

describe('AppSelectionService', () => {
  let service: AppSelectionService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AppSelectionService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
