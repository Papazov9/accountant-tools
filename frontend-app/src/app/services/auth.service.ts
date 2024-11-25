import {Injectable} from '@angular/core';
import {BehaviorSubject, catchError, interval, map, Observable, of, Subscription, throwError} from "rxjs";
import {HttpClient} from "@angular/common/http";
import {Router} from "@angular/router";
import {jwtDecode} from "jwt-decode";
import {JwtHelperService} from "@auth0/angular-jwt";

export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  username: string;
  password: string;
  gender: string;
}

export interface RegisterResponse {
  message: string;
  username: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  message: string;
  token: string;
  username: string;
}

export interface CurrentUser {
  username: string;
  roles: string[];
}

export interface CustomJWTPayload {
  sub: string;
  roles: string[];
  exp?: number;
}


@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private API_URL = "http://localhost:8080/api/auth";
  public TOKEN_KEY = "authToken";
  private currentUser: BehaviorSubject<CurrentUser | null>;
  private tokenCheckSubscription: Subscription | null = null;
  private checkInterval = 10 * 60 * 1000; // Check every 10 minutes
  private jwtHelper = new JwtHelperService();

  constructor(private http: HttpClient, private router: Router) {
    const token = this.getToken();
    const user = token ? this.decodeToken(token) : null;
    this.currentUser = new BehaviorSubject<CurrentUser | null>(user);
  }

  get currentUser$(): Observable<CurrentUser | null> {
    return this.currentUser.asObservable();
  }

  isAuthenticated(): boolean {
    this.checkTokenExpiration();
    return !!this.getToken();
  }

  login(loginRequest: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.API_URL}/login`, loginRequest).pipe(
      map(response => {
        this.setToken(response.token, true);
        this.startTokenCheck();
        const user = this.decodeToken(response.token);
        this.currentUser.next(user);
        this.router.navigate(['/dashboard']);
        return response;
      }),
      catchError(error => {
        console.error("Login error!", error);
        return throwError(() => new Error("Login failed!"));
      })
    );
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem('selectedApp');
    sessionStorage.removeItem(this.TOKEN_KEY);
    sessionStorage.removeItem("admin-access");
    this.currentUser.next(null);
    this.stopTokenCheck();
    this.router.navigate(['/']);
  }

  register(registerRequest: RegisterRequest): Observable<RegisterResponse> {
    return this.http.post<RegisterResponse>(`${this.API_URL}/register`, registerRequest).pipe(
      map(response => {
        this.router.navigate(['/confirm-code'], {queryParams: {email: registerRequest.email}});
        return response;
      }),
      catchError(error => {
        console.error("Registration failed", error);
        return throwError(() => new Error("Register failed!"));
      })
    )
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY) || sessionStorage.getItem(this.TOKEN_KEY);
  }

  isRememberMeChecked(): boolean {
    return !!localStorage.getItem(this.TOKEN_KEY);
  }

  setToken(token: string, rememberMe: boolean): void {
    if (rememberMe) {
      localStorage.setItem(this.TOKEN_KEY, token);
    } else {
      sessionStorage.setItem(this.TOKEN_KEY, token);
    }
  }

  private decodeToken(token: string): CurrentUser | null {
    try {
      const decodedToken = jwtDecode<CustomJWTPayload>(token);
      return {
        username: decodedToken.sub,
        roles: decodedToken.roles || []
      };
    } catch (error) {
      console.error(error);
      return null;
    }
  }

  refreshToken(): Observable<string> {
    const currentUser = this.currentUser.getValue();

    if (currentUser) {
      const username = currentUser.username;

      return this.http.post(`${this.API_URL}/refresh`, {username}, {responseType: "text"}).pipe(
        map(response => {
          console.log(response);
          return response;
        }),
        catchError(() => {
          this.logout();  // Log out if token refresh fails
          return throwError(() => new Error('Token refresh failed, please login again.'));
        })
      );
    } else {
      this.logout();
      return of('');
    }
  }

  startTokenCheck(): void {
    console.log("Start checking!");
    if (this.tokenCheckSubscription) {
      console.log("Already checked!");
      return; // Already running
    }

    this.tokenCheckSubscription = interval(this.checkInterval).subscribe(() => {
      this.checkTokenExpiration();
      console.log(this.tokenCheckSubscription);
    });
  }

  stopTokenCheck(): void {
    console.log("Stop!");
    if (this.tokenCheckSubscription) {
      this.tokenCheckSubscription.unsubscribe();
      this.tokenCheckSubscription = null;
      console.log("Token subscription cleared successfully!")
    }
  }

  private checkTokenExpiration(): void {
    const token = this.getToken();
    if (token) {
      const isTokenExpired = this.jwtHelper.isTokenExpired(token);
      const tokenWillExpireSoon = this.jwtHelper.isTokenExpired(token, 300); // 5 min

      if (isTokenExpired) {
        this.logout(); // Log out if token is already expired
      } else if (tokenWillExpireSoon) {
        this.refreshToken().subscribe({
          next: (newToken: string) => {
            this.setToken(newToken, this.isRememberMeChecked());
          },
          error: () => {
            this.logout();
          }
        });
      }
    }
  }

  confirmEmail(email: string, code: any): Observable<any> {
    return this.http.post(`${this.API_URL}/confirm-code`, {email, code});
  }
}
