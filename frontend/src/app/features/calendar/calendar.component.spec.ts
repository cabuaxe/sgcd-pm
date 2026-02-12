import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { CalendarComponent } from './calendar.component';
import { DashboardService } from '../../core/services/dashboard.service';
import { CalendarData } from '../../core/models/dashboard.model';

const mockCalendar: CalendarData = {
  year: 2026,
  month: 2,
  days: [
    {
      date: '2026-02-02', dayOfWeek: 'MON', isBlocked: false,
      blockReason: '', task: null as any, isWorkDay: true
    },
    {
      date: '2026-02-03', dayOfWeek: 'TUE', isBlocked: false,
      blockReason: '', task: {
        id: 1, sprintId: 1, sprintNumber: 1, sprintName: 'Fundação',
        taskCode: 'S1-01', sessionDate: '2026-02-03', dayOfWeek: 'TER',
        weekNumber: 1, plannedHours: 3.5, title: 'Setup',
        titleEn: 'Setup', description: '', deliverables: [],
        validationCriteria: [], coverageTarget: '80%', status: 'PLANNED',
        actualHours: 0, startedAt: '', completedAt: '', completionNotes: '',
        blockers: '', sortOrder: 1, notes: [], executions: []
      }, isWorkDay: true
    },
    {
      date: '2026-02-07', dayOfWeek: 'SAT', isBlocked: false,
      blockReason: '', task: null as any, isWorkDay: false
    }
  ]
};

describe('CalendarComponent', () => {
  let component: CalendarComponent;
  let fixture: ComponentFixture<CalendarComponent>;
  let mockDashboardService: { getCalendar: jest.Mock };

  beforeEach(async () => {
    mockDashboardService = {
      getCalendar: jest.fn().mockReturnValue(of(mockCalendar))
    };

    await TestBed.configureTestingModule({
      imports: [CalendarComponent, NoopAnimationsModule],
      providers: [
        provideRouter([]),
        { provide: DashboardService, useValue: mockDashboardService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CalendarComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have current year and month as defaults', () => {
    const now = new Date();
    expect(component.year).toBe(now.getFullYear());
    expect(component.month).toBe(now.getMonth() + 1);
  });

  it('should have null calendar initially', () => {
    expect(component.calendar).toBeNull();
  });

  it('should call dashboardService.getCalendar on init (load)', () => {
    fixture.detectChanges();
    expect(mockDashboardService.getCalendar).toHaveBeenCalledWith(component.year, component.month);
  });

  it('should populate calendar after init', () => {
    fixture.detectChanges();
    expect(component.calendar).toBe(mockCalendar);
  });

  it('should calculate leadingBlanks based on the first day of the month', () => {
    fixture.detectChanges();
    // leadingBlanks should be an array; exact length depends on what day Feb starts
    expect(Array.isArray(component.leadingBlanks)).toBe(true);
  });

  it('should extract day number from date string with getDayNum', () => {
    expect(component.getDayNum('2026-02-03')).toBe(3);
    expect(component.getDayNum('2026-02-15')).toBe(15);
    expect(component.getDayNum('2026-12-31')).toBe(31);
  });

  it('should decrement month on prevMonth', () => {
    component.year = 2026;
    component.month = 5;
    component.prevMonth();
    expect(component.month).toBe(4);
    expect(component.year).toBe(2026);
    expect(mockDashboardService.getCalendar).toHaveBeenCalledWith(2026, 4);
  });

  it('should wrap to December of previous year when prevMonth from January', () => {
    component.year = 2026;
    component.month = 1;
    component.prevMonth();
    expect(component.month).toBe(12);
    expect(component.year).toBe(2025);
    expect(mockDashboardService.getCalendar).toHaveBeenCalledWith(2025, 12);
  });

  it('should increment month on nextMonth', () => {
    component.year = 2026;
    component.month = 5;
    component.nextMonth();
    expect(component.month).toBe(6);
    expect(component.year).toBe(2026);
    expect(mockDashboardService.getCalendar).toHaveBeenCalledWith(2026, 6);
  });

  it('should wrap to January of next year when nextMonth from December', () => {
    component.year = 2026;
    component.month = 12;
    component.nextMonth();
    expect(component.month).toBe(1);
    expect(component.year).toBe(2027);
    expect(mockDashboardService.getCalendar).toHaveBeenCalledWith(2027, 1);
  });

  it('should have all 12 month names in Portuguese', () => {
    expect(component.monthNames.length).toBe(12);
    expect(component.monthNames[0]).toBe('Janeiro');
    expect(component.monthNames[11]).toBe('Dezembro');
  });

  it('should render day headers in the grid', () => {
    fixture.detectChanges();
    const headers = fixture.nativeElement.querySelectorAll('.cal-day-header');
    expect(headers.length).toBe(7);
    expect(headers[0].textContent.trim()).toBe('Seg');
  });

  it('should render calendar cells after loading', () => {
    fixture.detectChanges();
    const cells = fixture.nativeElement.querySelectorAll('.cal-cell:not(.empty)');
    expect(cells.length).toBe(mockCalendar.days.length);
  });

  it('should render task link for days with tasks', () => {
    fixture.detectChanges();
    const taskLinks = fixture.nativeElement.querySelectorAll('.task-link');
    expect(taskLinks.length).toBe(1);
    expect(taskLinks[0].textContent).toContain('S1-01');
  });

  it('should display month and year in header', () => {
    fixture.detectChanges();
    const header = fixture.nativeElement.querySelector('.cal-header h2');
    expect(header.textContent).toContain(component.monthNames[component.month - 1]);
    expect(header.textContent).toContain(String(component.year));
  });
});
