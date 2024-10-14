import { Injectable } from '@angular/core';
import {BehaviorSubject, Observable} from "rxjs";
import {AuthService, CurrentUser} from "./auth.service";
import {HttpClient} from "@angular/common/http";

export interface UserProfile {
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  birthDate: string;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private API_URL = 'http://localhost:8080/api/user';

  constructor(private http: HttpClient, private authService: AuthService) {}

  fetchUserProfile(): Observable<UserProfile> {
    return new Observable<UserProfile>((observer) => {
      this.authService.currentUser$.subscribe((currentUser: CurrentUser | null) => {
        if (currentUser?.username) {
          this.http.get<UserProfile>(`${this.API_URL}/dashboard/${currentUser.username}`).subscribe(
            (userProfile) => {
              observer.next(userProfile);
              observer.complete();
            },
            (error) => {
              observer.error(error);
            }
          );
        } else {
          observer.error("User not logged in!");
        }
      });
    });
  }
}
