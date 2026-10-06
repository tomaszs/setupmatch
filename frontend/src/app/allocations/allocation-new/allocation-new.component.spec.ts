import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { provideRouter } from '@angular/router';
import { AllocationNewComponent } from './allocation-new.component';
import { Equipment } from '../../models/equipment.model';

const equipmentFixture: Equipment[] = [
  {
    id: '1',
    type: 'main_computer',
    brand: 'Apple',
    model: 'MacBook Pro 14',
    state: 'available',
    condition_score: 0.95,
    purchase_date: '2024-06-01',
    retire_reason: null,
    retired_at: null,
  },
  {
    id: '2',
    type: 'monitor',
    brand: 'Dell',
    model: 'U2723QE',
    state: 'available',
    condition_score: 0.9,
    purchase_date: '2024-01-10',
    retire_reason: null,
    retired_at: null,
  },
];

describe('AllocationNewComponent', () => {
  let fixture: ComponentFixture<AllocationNewComponent>;
  let component: AllocationNewComponent;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AllocationNewComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        provideNoopAnimations(),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(AllocationNewComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    fixture.detectChanges();

    httpMock.expectOne('/api/equipments').flush(equipmentFixture);
    fixture.detectChanges();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('shows brand pills from inventory for the selected slot type', () => {
    expect(component.brandPills(0)).toEqual(['Dell']);

    component.setSlotType(0, 'main_computer');
    expect(component.brandPills(0)).toEqual(['Apple']);
  });

  it('suggests demo employee ids', () => {
    component.form.controls.employee_id.setValue('emp-qa');

    expect(component.employeeSuggestions()).toContain('emp-qa-1');
  });
});
