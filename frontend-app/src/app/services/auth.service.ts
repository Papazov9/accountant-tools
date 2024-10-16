import {Injectable} from '@angular/core';
import {BehaviorSubject, catchError, map, Observable, of, tap, throwError} from "rxjs";
import {HttpClient} from "@angular/common/http";
import {Router} from "@angular/router";
import {jwtDecode, JwtPayload} from "jwt-decode";

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
  private lastActivityTime: number;

  constructor(private http: HttpClient, private router: Router) {
    const token = this.getToken();
    const user = token ? this.decodeToken(token) : null;
    this.currentUser = new BehaviorSubject<CurrentUser | null>(user);
    this.trackUserActivity();
    this.lastActivityTime = this.getLastActivityTime();
  }

  private trackUserActivity() {
    ['click', 'keydown', 'mousemove'].forEach(event => {
      window.addEventListener(event, () => this.updateLastActivity());
    });
  }

  private updateLastActivity() {
    this.lastActivityTime = Date.now();
  }

  getLastActivityTime() {
    return this.lastActivityTime;
  }

  get currentUser$(): Observable<CurrentUser | null> {
    return this.currentUser.asObservable();
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  login(loginRequest: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.API_URL}/login`, loginRequest);
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    sessionStorage.removeItem(this.TOKEN_KEY);
    this.currentUser.next(null);
    this.router.navigate(['/']);
  }

  register(registerRequest: RegisterRequest): Observable<RegisterResponse> {
    return this.http.post<RegisterResponse>(`${this.API_URL}/register`, registerRequest);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY) || sessionStorage.getItem(this.TOKEN_KEY);
  }

  setToken(token: string, rememberMe: boolean): void {
    if (rememberMe) {
      localStorage.setItem(this.TOKEN_KEY, token);
    } else {
      sessionStorage.setItem(this.TOKEN_KEY, token);
    }
    const user = this.decodeToken(token);
    this.currentUser.next(user);
  }

  private decodeToken(token: string): CurrentUser | null {
    try {
      const decodedToken = jwtDecode<CustomJWTPayload>(token);
      return {
        username: decodedToken.sub,
        roles: decodedToken.roles || []
      };
    } catch (error) {
      console.error("Invalid JWT token", error);
      return null;
    }
  }

  refreshToken(): Observable<string> {
    const currentUser =   this.currentUser.getValue();
    let username: string;
    if (currentUser) {
      username = currentUser.username;
    }else {
      this.logout();
      return of("");
    }
    return this.http.post(`${this.API_URL}/refresh`, {username}, {responseType: "text"}).pipe(
      catchError((error) => {
        this.logout();
        return throwError(() => new Error('Token refresh failed, please login again.'));
      })
    )
  }
}
