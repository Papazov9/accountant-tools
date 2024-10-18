import {Injectable} from '@angular/core';
import {BehaviorSubject, map, Observable, switchMap, throwError} from "rxjs";
import {AuthService, CurrentUser} from "./auth.service";
import {HttpClient} from "@angular/common/http";

export interface UserProfile {
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  gender: string;
}

export interface ComparisonMetric {
  mismatches: number;
  comparisonDate: string;
}

export interface Metrics {
  message: string;
  empty: boolean;
  comparisonCount: number;
  totalComparisons: number;
  successfulComparisons: number;
  failedComparisons: number;
  totalMismatches: number;
  comparisonMetricResponses: ComparisonMetric[];
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private API_URL = 'http://localhost:8080/api/user';
  private METRICS_URL = 'http://localhost:8080/api/metrics';

  constructor(private http: HttpClient, private authService: AuthService) {
  }

  fetchUserProfile(): Observable<UserProfile> {
    return this.authService.currentUser$.pipe(
      switchMap((currentUser: CurrentUser | null) => {
        if (currentUser?.username) {
          return this.http.get<UserProfile>(`${this.API_URL}/dashboard/${currentUser.username}`);
        } else {
          return throwError(() => new Error("User not logged in!"));
        }
      })
    );
  }

  loadMetrics(username: string): Observable<Metrics> {
    if (!username) {
      this.authService.logout();
    }
    return this.http.get<Metrics>(`${this.METRICS_URL}/all/${username}`);
  }
}
