import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-schedule-interview',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './schedule-interview.component.html',
  styleUrl: './schedule-interview.component.css'
})
export class ScheduleInterviewComponent {
    readonly MAX_DAY = 30;   // ← September has 30 days , to be changed later
  candidate = {
    name: 'John Doe',
    role: 'Principal Product Architect',
    location: 'San Francisco, CA',
    experience: '12+ Years Experience',
    avatar: 'https://i.pravatar.cc/150?img=7',
    email: 'john.doe@techgiant.com',
    skills: ['Distributed Systems', 'Strategic Vision', 'FinTech'],
    interviewContext:
      'John is moving into the Final Executive Round. Focus on cultural alignment and technical scalability vision.'
  };

  dayLabels = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];

  calendarDays = [
    { date: 24, disabled: true },
    { date: 25, disabled: true },
    { date: 26, disabled: false },
    { date: 27, disabled: false },
    { date: 28, disabled: false },
    { date: 29, disabled: false },
    { date: 30, disabled: false }
  ];

  timeSlots = [
    { time: '09:00 AM', duration: '60 mins' },
    { time: '11:30 AM', duration: '60 mins' },
    { time: '02:15 PM', duration: '60 mins' }
  ];

  selectedDate: number = 29;
  selectedSlot: string = '09:00 AM';
  meetLink: string = 'meet.google.com/abc-defg-hij';
  copied: boolean = false;
  showToast: boolean = true;

  selectDate(date: number) { this.selectedDate = date; }
  selectSlot(slot: string) { this.selectedSlot = slot; }

  generateMeetLink() {
    const chars = 'abcdefghijklmnopqrstuvwxyz';
    const rand = (n: number) => Array.from({length: n}, () => chars[Math.floor(Math.random() * chars.length)]).join('');
    this.meetLink = `meet.google.com/${rand(3)}-${rand(4)}-${rand(3)}`;
  }

  copyMeetLink() {
    navigator.clipboard.writeText(this.meetLink);
    this.copied = true;
    setTimeout(() => this.copied = false, 2000);
  }

  
  prevWeek() {
    const first = this.calendarDays[0].date;
    const newFirst = first - 7;

    if (newFirst < 1) {
      return; // already at the beginning of the month → do nothing
    }

    this.calendarDays = this.calendarDays.map(d => ({ ...d, date: d.date - 7 }));

    if (this.selectedDate >= first) {
      this.selectedDate = this.calendarDays[0].date;
    }
  }

  nextWeek() {
    const last = this.calendarDays[this.calendarDays.length - 1].date;
    const newFirst = this.calendarDays[0].date + 7;

    if (newFirst > this.MAX_DAY) {
      return; // already at the last week of September → do nothing
    }

    this.calendarDays = this.calendarDays.map(d => ({ ...d, date: d.date + 7 }));

    if (this.selectedDate <= last) {
      this.selectedDate = this.calendarDays[0].date;
    }
  }

  sendInvitation() { this.showToast = true; }
}
