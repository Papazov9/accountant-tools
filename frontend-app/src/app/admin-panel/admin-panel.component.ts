import { Component } from '@angular/core';
import {UserService} from "../services/user.service";
import {RouterLink} from "@angular/router";

@Component({
  selector: 'app-admin-panel',
  standalone: true,
  imports: [
    RouterLink
  ],
  templateUrl: './admin-panel.component.html',
  styleUrl: './admin-panel.component.css'
})
export class AdminPanelComponent {

  constructor() {
  }
}
