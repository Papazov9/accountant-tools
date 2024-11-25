import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AppSelectionOverlayComponent } from './app-selection-overlay.component';

describe('AppSelectionOverlayComponent', () => {
  let component: AppSelectionOverlayComponent;
  let fixture: ComponentFixture<AppSelectionOverlayComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppSelectionOverlayComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AppSelectionOverlayComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
