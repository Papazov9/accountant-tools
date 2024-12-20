import {Injectable} from '@angular/core';
import {BehaviorSubject, catchError, map, Observable, of, switchMap, take, tap, throwError} from "rxjs";
import {AuthService} from "./auth.service";
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
  private userProfileSubject = new BehaviorSubject<UserProfile | null>(null);

  constructor(private http: HttpClient, private authService: AuthService) {
  }

  get userProfile$(): Observable<UserProfile | null> {
    return this.userProfileSubject.asObservable();
  }

  fetchUserProfile(): void {
   this.authService.currentUser$.pipe(
     switchMap(currentUser => {
       if (currentUser && currentUser.username){
         return this.http.get<UserProfile>(`${this.API_URL}/dashboard/${currentUser.username}`);
       }
       return of(null);
     }),
     tap(profile => {
       if (profile) {
         this.userProfileSubject.next(profile);
       }else {
         console.log("User not logged in!");
       }
     })
   ).subscribe();
  }

  loadMetrics(username: string): Observable<Metrics> {
    if (!username) {
      this.authService.logout();
    }
    return this.http.get<Metrics>(`${this.METRICS_URL}/all/${username}`);
  }

  acceptFreeTrial() {
    return this.userProfile$.pipe(
      take(1),
      switchMap((currentUser: UserProfile | null) => {
        if (currentUser?.username) {
          return this.http.get<string>(`${this.API_URL}/free/${currentUser.email}`, {responseType: 'text' as 'json'}).pipe(
            tap(() => this.fetchUserProfile()),
            catchError((error) => {
              console.error(error);
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
