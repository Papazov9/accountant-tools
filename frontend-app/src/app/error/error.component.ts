import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";

@Component({
  selector: 'app-error',
  standalone: true,
  imports: [],
  templateUrl: './error.component.html',
  styleUrl: './error.component.css'
})
export class ErrorComponent implements OnInit {
  errorTitle: string = 'Oops!';
  errorMessage: string = 'Something went wrong. Please try again later.';
  imageSource: string = 'assets/images/generic-error.svg'; // Default image

  constructor(private route: ActivatedRoute, private router: Router) {}

  ngOnInit(): void {
    const errorCode = this.route.snapshot.paramMap.get('code');
    console.log(errorCode);
    this.setupErrorPage(errorCode);
  }

  setupErrorPage(errorCode: string | null) {
    switch (errorCode) {
      case '400':
        this.errorTitle = 'Bad Request (400)';
        this.errorMessage = 'There was an issue with your request.';
        this.imageSource = 'assets/images/paper-shredder-error.svg'; // Custom image for 400
        break;
      case '401':
        this.errorTitle = 'Unauthorized (401)';
        this.errorMessage = 'You are not authorized to view this page.';
        this.imageSource = 'assets/images/lock-error.svg'; // Custom image for 401
        break;
      case '404':
        this.errorTitle = 'Page Not Found (404)';
        this.errorMessage = 'The page you are looking for doesn’t exist.';
        this.imageSource = 'assets/images/404-paper-error.svg'; // Custom image for 404
        break;
      case '500':
        this.errorTitle = 'Internal Server Error (500)';
        this.errorMessage = 'We are experiencing server issues. Please try again later.';
        this.imageSource = 'assets/images/server-error.svg'; // Custom image for 500
        break;
      default:
        this.errorTitle = 'Oops!';
        this.errorMessage = 'Something went wrong. Please try again later.';
        this.imageSource = 'assets/images/generic-error.svg'; // Default image
        break;
    }
  }

  goToDashboard() {
    this.router.navigate(['/dashboard']);
  }
}
