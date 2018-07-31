import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Observable} from "rxjs/index";
import {User} from "../profile/user/user";
import {UserAnswers} from "./user-test/user-answers";



const httpOptions = {
  headers: new HttpHeaders({
    'Content-Type':  'application/json',
    // 'Authorization': 'my-auth-token'
  })
};
@Injectable({
  providedIn: 'root'
})
export class TestService {
  testUrl = 'http://localhost:4200/api/test/testlist';  // URL to web api

  constructor(private http: HttpClient) {

  }


  /** GET tests list from the server */
  getTestList(): Observable<UserAnswers> {
    return this.http.get<UserAnswers>(this.testUrl);
    // .pipe(
    //     catchError(this.handleError('getTestList', []))
    //   );
  }
}
