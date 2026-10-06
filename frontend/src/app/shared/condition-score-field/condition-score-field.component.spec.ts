import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ConditionScoreFieldComponent } from './condition-score-field.component';

describe('ConditionScoreFieldComponent', () => {
  let fixture: ComponentFixture<ConditionScoreFieldComponent>;
  let component: ConditionScoreFieldComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ConditionScoreFieldComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ConditionScoreFieldComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('adjusts score in 0.01 steps', () => {
    component.writeValue(0.5);
    component.adjust(0.01);

    expect(component.value).toBe(0.51);
  });
});
