import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {map, Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class LocationService {

  private readonly COUNTRIES_API = 'https://restcountries.com/v3.1/region/europe';
  private countryCode = '';
  private readonly CITIES_API = `https://api.countrystatecity.in/v1/countries/${this.countryCode}/cities`;
  private readonly POSTAL_CODES_API = 'https://zip-api.eu/api/v1/codes/';
  private API_KEY = 'X-CSCAPI-KEY';

  constructor(private http: HttpClient) { }

  getCountries(): Observable<string[]> {
    const headers = new HttpHeaders({
      'X-CSCAPI-KEY': this.API_KEY // Set the header with the required API key
    });
    headers.append("API_KEY", this.API_KEY);
    return this.http.get<any[]>(this.COUNTRIES_API, { headers }).pipe(
      map((countries) => countries.map(country => country.name.common).sort())
    );
  }
}
