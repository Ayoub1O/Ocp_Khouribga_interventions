import { Component } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { FullCalendarModule } from '@fullcalendar/angular';
import { CalendarOptions } from '@fullcalendar/core';
import dayGridPlugin from '@fullcalendar/daygrid';
import interactionPlugin from '@fullcalendar/interaction';
import timeGridPlugin from '@fullcalendar/timegrid';
import { interventions } from '../../../core/api/mock-data';

@Component({
  selector: 'app-interventions-page',
  imports: [MatButtonModule, FullCalendarModule],
  templateUrl: './interventions.page.html',
  styleUrl: './interventions.page.scss',
})
export class InterventionsPage {
  protected readonly interventions = interventions;
  protected readonly calendarOptions: CalendarOptions = {
    plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin],
    initialView: 'timeGridDay',
    height: 620,
    headerToolbar: {
      left: 'prev,next today',
      center: 'title',
      right: 'dayGridMonth,timeGridWeek,timeGridDay',
    },
    locale: 'fr',
    events: interventions.map((intervention) => ({
      id: intervention.id,
      title: `${intervention.ticket} - ${intervention.technicien}`,
      start: intervention.debut,
      end: intervention.fin,
    })),
  };
}
