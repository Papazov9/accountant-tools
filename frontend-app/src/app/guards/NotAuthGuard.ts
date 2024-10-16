
import {AuthService} from "../services/auth.service";
import {CanActivate, Router} from "@angular/router";
import {Injectable} from "@angular/core";
@Injectable({
  providedIn: 'root'
})
export class NotAuthGuard implements CanActivate {

  constructor(private authService: AuthService, private router: Router) { }

    canActivate(): boolean {
        if (this.authService.isAuthenticated()) {
          this.router.navigate(['/dashboard']);
          return false;
        }
        return true;
    }

}
