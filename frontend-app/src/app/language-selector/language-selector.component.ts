import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {NgIf} from "@angular/common";

@Component({
  selector: 'app-language-selector',
  standalone: true,
  imports: [
    NgIf
  ],
  templateUrl: './language-selector.component.html',
  styleUrl: './language-selector.component.css'
})
export class LanguageSelectorComponent implements OnInit{
  @Output() languageSelected = new EventEmitter<string>();
  showLanguageSelector = true;

  ngOnInit() {
    const preferredLanguage = localStorage.getItem('preferredLanguage');
    if (preferredLanguage) {
      this.showLanguageSelector = false;
    }
  }

  selectLanguage(language: string) {
    localStorage.setItem('preferredLanguage', language);
    this.languageSelected.emit(language);
    this.showLanguageSelector = false; // Hide the popup after selection
    window.location.href = `/${language}`; // Reload the app with the selected language path
  }
}
