import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';

interface ScheduleInterviewRequest {
  candidateName:   string;
  candidateEmail:  string;
  recruiterEmail:  string;
  startDateTime:   string;
  durationMinutes: number;
  interviewTitle:  string;
}

interface ScheduleInterviewResponse {
  calendarEventId: string;
  meetLink:        string;
  startDateTime:   string;
  endDateTime:     string;
  invitationSent:  boolean;
}

interface CalendarDay {
  date:    number;
  month:   number;
  year:    number;
  isToday: boolean;
  isPast:  boolean;
  isEmpty: boolean;
}

@Component({
  selector: 'app-schedule-interview',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './schedule-interview.component.html',
  styleUrl: './schedule-interview.component.css'
})
export class ScheduleInterviewComponent implements OnInit {

  private readonly API        = 'http://localhost:9091/api/interviews';
  private readonly GOOGLE_API = 'http://localhost:9091/api/google';

  candidate = {
    name:           'John Doe',
    role:           'Principal Product Architect',
    location:       'San Francisco, CA',
    experience:     '12+ Years Experience',
    avatar:         'https://i.pravatar.cc/150?img=7',
    email:          'oussemaguerriche@gmail.com',
    recruiterEmail: 'oussemaguerriche1@gmail.com',
    skills:         ['Distributed Systems', 'Strategic Vision', 'FinTech'],
    interviewContext:
      'John is moving into the Final Executive Round. Focus on cultural alignment and technical scalability vision.'
  };

  dayLabels  = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
  monthNames = [
    'January','February','March','April','May','June',
    'July','August','September','October','November','December'
  ];

  currentMonth!: number;
  currentYear!:  number;
  calendarDays:  CalendarDay[] = [];
  selectedDay:   CalendarDay | null = null;

  timeSlots = [
    { time: '09:00', display: '09:00 AM', duration: '60 mins' },
    { time: '11:30', display: '11:30 AM', duration: '60 mins' },
    { time: '14:15', display: '02:15 PM', duration: '60 mins' }
  ];
  selectedSlot = '09:00';

  meetLink:        string  = '';
  copied:          boolean = false;
  showToast:       boolean = false;
  isLoading:       boolean = false;
  errorMessage:    string  = '';
  calendarEventId: string  = '';

  // Google Calendar connection state
  googleNotConnected: boolean = false;
  isConnecting:       boolean = false;

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    const today       = new Date();
    this.currentMonth = today.getMonth();
    this.currentYear  = today.getFullYear();
    this.buildCalendar();
  }

  buildCalendar(): void {
    const today    = new Date();
    const firstDay = new Date(this.currentYear, this.currentMonth, 1);
    const lastDay  = new Date(this.currentYear, this.currentMonth + 1, 0);

    let startOffset = firstDay.getDay() - 1;
    if (startOffset < 0) startOffset = 6;

    this.calendarDays = [];

    for (let i = 0; i < startOffset; i++) {
      this.calendarDays.push({ date: 0, month: 0, year: 0, isToday: false, isPast: false, isEmpty: true });
    }

    for (let d = 1; d <= lastDay.getDate(); d++) {
      const thisDate  = new Date(this.currentYear, this.currentMonth, d);
      const todayDate = new Date(today.getFullYear(), today.getMonth(), today.getDate());
      this.calendarDays.push({
        date: d, month: this.currentMonth, year: this.currentYear,
        isToday: thisDate.toDateString() === today.toDateString(),
        isPast:  thisDate < todayDate,
        isEmpty: false
      });
    }

    const remaining = 42 - this.calendarDays.length;
    for (let i = 0; i < remaining; i++) {
      this.calendarDays.push({ date: 0, month: 0, year: 0, isToday: false, isPast: false, isEmpty: true });
    }
  }

  get currentMonthLabel(): string {
    return `${this.monthNames[this.currentMonth]} ${this.currentYear}`;
  }

  get canGoPrev(): boolean {
    const today = new Date();
    return !(this.currentMonth === today.getMonth() && this.currentYear === today.getFullYear());
  }

  prevMonth(): void {
    if (!this.canGoPrev) return;
    if (this.currentMonth === 0) { this.currentMonth = 11; this.currentYear--; }
    else { this.currentMonth--; }
    this.selectedDay = null;
    this.buildCalendar();
  }

  nextMonth(): void {
    if (this.currentMonth === 11) { this.currentMonth = 0; this.currentYear++; }
    else { this.currentMonth++; }
    this.selectedDay = null;
    this.buildCalendar();
  }

  selectDay(day: CalendarDay): void {
    if (day.isEmpty || day.isPast) return;
    this.selectedDay = day;
  }

  isDaySelected(day: CalendarDay): boolean {
    if (!this.selectedDay || day.isEmpty) return false;
    return day.date === this.selectedDay.date &&
           day.month === this.selectedDay.month &&
           day.year === this.selectedDay.year;
  }

  get selectedDateLabel(): string {
    if (!this.selectedDay) return 'Select a date';
    return `${this.monthNames[this.selectedDay.month].slice(0, 3).toUpperCase()} ${this.selectedDay.date}`;
  }

  selectSlot(slot: string): void { this.selectedSlot = slot; }

  private buildStartDateTime(): string {
    if (!this.selectedDay) return '';
    const m = (this.selectedDay.month + 1).toString().padStart(2, '0');
    const d = this.selectedDay.date.toString().padStart(2, '0');
    return `${this.selectedDay.year}-${m}-${d}T${this.selectedSlot}:00`;
  }

  copyMeetLink(): void {
    navigator.clipboard.writeText(this.meetLink);
    this.copied = true;
    setTimeout(() => this.copied = false, 2000);
  }

  // ── Google Calendar Connection ─────────────────────────────────────────────

  connectGoogleCalendar(): void {
    this.isConnecting = true;
    this.errorMessage = '';

    const token   = localStorage.getItem('token');
    const headers = new HttpHeaders({ 'Authorization': `Bearer ${token}` });

    this.http.get<{ url: string }>(`${this.GOOGLE_API}/connect`, { headers })
      .subscribe({
        next: (res) => {
          this.isConnecting = false;
          // Open Google consent in a new tab — recruiter approves there
          window.open(res.url, '_blank');
          // Update UI to guide the recruiter
          this.googleNotConnected = false;
          this.errorMessage = 'Please approve access in the new tab, then click "Send Invitation" again.';
        },
        error: (err) => {
          this.isConnecting = false;
          this.errorMessage = err.error?.error ?? 'Failed to get Google authorization URL.';
        }
      });
  }

  // ── Send Invitation ────────────────────────────────────────────────────────

  sendInvitation(): void {
    if (!this.selectedDay) {
      this.errorMessage = 'Please select a date first.';
      return;
    }

    this.isLoading          = true;
    this.errorMessage       = '';
    this.googleNotConnected = false;

    const token   = localStorage.getItem('token');
    const headers = new HttpHeaders({
      'Content-Type':  'application/json',
      'Authorization': `Bearer ${token}`
    });

    const body: ScheduleInterviewRequest = {
      candidateName:   this.candidate.name,
      candidateEmail:  this.candidate.email,
      recruiterEmail:  this.candidate.recruiterEmail,
      startDateTime:   this.buildStartDateTime(),
      durationMinutes: 60,
      interviewTitle:  `Interview – ${this.candidate.name}`
    };

    this.http.post<ScheduleInterviewResponse>(this.API, body, { headers })
      .subscribe({
        next: (res) => {
          this.isLoading          = false;
          this.meetLink           = res.meetLink;
          this.calendarEventId    = res.calendarEventId;
          this.showToast          = true;
          this.googleNotConnected = false;
        },
        error: (err) => {
          this.isLoading = false;
          const message: string = err.error?.error ?? err.error ?? '';

          // Detect "not connected" specifically → show connect banner
          if (message.toLowerCase().includes('not connected') ||
              message.toLowerCase().includes('has not connected')) {
            this.googleNotConnected = true;
          } else {
            this.errorMessage = message || 'An error occurred. Please try again.';
          }
        }
      });
  }

  cancelInterview(): void {
    if (!this.calendarEventId) return;

    const token   = localStorage.getItem('token');
    const headers = new HttpHeaders({ 'Authorization': `Bearer ${token}` });

    this.http.delete(`${this.API}/${this.calendarEventId}`, { headers })
      .subscribe({
        next: () => {
          this.showToast       = false;
          this.meetLink        = '';
          this.calendarEventId = '';
          this.selectedDay     = null;
        },
        error: (err) => {
          this.errorMessage = err.error?.error ?? 'Failed to cancel.';
        }
      });
  }
}