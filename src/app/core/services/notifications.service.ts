import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Notification } from '../models/notification.model';

@Injectable({ providedIn: 'root' })
export class NotificationsService {
  private apiUrl = `${environment.apiUrl}/notifications`;

  constructor(private http: HttpClient) {}

  listNotifications(): Observable<Notification[]> {
    // No default mock data in views: return empty list.
    // TODO: Implement real API call to fetch notifications (e.g. this.http.get<Notification[]>(this.apiUrl)).
    return of([] as Notification[]);
  }

  markAsRead(notificationId: string): Observable<Notification> {
    // TODO: Implement mark-as-read endpoint on backend
    return this.http.put<Notification>(`${this.apiUrl}/${notificationId}/read`, {});
  }

  dismissNotification(notificationId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${notificationId}`);
  }

}
