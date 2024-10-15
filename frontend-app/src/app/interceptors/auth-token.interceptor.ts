import {HttpInterceptorFn} from '@angular/common/http';
import {inject} from "@angular/core";
import {AuthService} from "../services/auth.service";
import {JwtHelperService} from '@auth0/angular-jwt';
import {catchError, switchMap, throwError} from "rxjs";

export const authTokenInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const isAuthenticated = authService.isAuthenticated();
  const token = authService.getToken();
  const jwtHelper = new JwtHelperService();

  if (isAuthenticated && token) {
    const isTokenExpired = jwtHelper.isTokenExpired(token);
    const lastActivity = authService.getLastActivityTime();
    const currentTime = Date.now();
    const MAX_INACTIVITY_TIME = 15 * 60 * 1000;

    if (isTokenExpired && (currentTime - lastActivity > MAX_INACTIVITY_TIME)) {
      authService.logout();
      alert("Session expired! Please login again!");
    }

    if (jwtHelper.isTokenExpired(token, 300)) {
      return authService.refreshToken().pipe(
        switchMap((newToken: string) => {
          authService.setToken(newToken, true);

          const clone = req.clone({
            setHeaders: {
              Authorization: `Bearer ${newToken}`
            }
          });

          return next(clone);
        }),
        catchError(()=> {
          authService.logout();
          return throwError(() => new Error('Session expired! Please login again!'));
        })
      )
    }

    const clonedRequest = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });

    return next(clonedRequest);
  }
  return next(req);
};
