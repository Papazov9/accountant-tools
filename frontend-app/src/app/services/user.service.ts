import {Injectable} from '@angular/core';
import {BehaviorSubject, catchError, map, Observable, of, switchMap, throwError} from "rxjs";
import {AuthService, CurrentUser} from "./auth.service";
import {HttpClient} from "@angular/common/http";
import {PricingPlan} from "../home/pricing-section/pricing-section.component";

export interface UserProfile {
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  gender: string;
  isAcknowledge: boolean;
  message: string;
  subscription: PricingPlan | null;
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

  fetchUserProfile(): Observable<UserProfile | null> {
    let username: string | null = null;
    this.authService.currentUser$.subscribe((currentUser) => {
      if (currentUser) {
        username = currentUser.username;
      }
    });
    if (username) {
      return this.http.get<UserProfile>(`${this.API_URL}/dashboard/${username}`);
    }

    return of(null);
  }

  loadMetrics(username: string): Observable<Metrics> {
    if (!username) {
      this.authService.logout();
    }
    return this.http.get<Metrics>(`${this.METRICS_URL}/all/${username}`);
  }

  acceptFreeTrial() {
    return this.authService.currentUser$.pipe(
      switchMap((currentUser: CurrentUser | null) => {
        if (currentUser?.username) {
          return this.http.get<string>(`${this.API_URL}/free/${currentUser.username}`, {responseType: 'text' as 'json'}).pipe(
            catchError((error) => {
              console.error("Error during free trial subscription:", error);
              return throwError(() => new Error("Subscription request failed."));
            })
          );
        } else {
          return throwError(() => new Error("User not logged in!"));
        }
      })
    );
  }
}
