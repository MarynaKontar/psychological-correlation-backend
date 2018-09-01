import { Injectable } from '@angular/core';
import {Observable} from "rxjs/index";
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {User} from "./user/user";

const httpOptions = {
  headers: new HttpHeaders({
    'Content-Type':  'application/json',
    // 'Authorization': 'my-auth-token'
  })
};

@Injectable({
  providedIn: 'root'
})
export class UserService {
  userUrl = 'http://localhost:4200/api/user';  // URL to web api

  constructor(private http: HttpClient) {
  }

  /** POST: add a new user to the server (database) */
  add(user: User): Observable<User> {
    const url='http://localhost:4200/api/user/add';
    return this.http.post<User>(url, user, httpOptions);
    // .pipe(
    //   catchError(this.handleError('add', user))
  }

  /** GET user from the server */
  getUser(userName: string): Observable<User> {
    const url = `${this.userUrl}/${userName}`;
    return this.http.get<User>(this.userUrl);
    // .pipe(
    //     catchError(this.handleError('getUsers', []))
    //   );
  }

  /** GET users from the server */
  getUsers(): Observable<User[]> {
    return this.http.get<User[]>('http://localhost:4200/api/user/getAll');
    // return this.http.get<User[]>(this.userUrl + "/getAll");
    // .pipe(
    //     catchError(this.handleError('getUsers', []))
    //   );
  }

  /** POST: deleteUser the user from the server */
  deleteUser(userName: string): Observable<{}> {
    const url = `${this.userUrl}/${userName}/"delete"`; // DELETE api/heroes/42
    return this.http.post(url, httpOptions)
    // .pipe(
    //   catchError(this.handleError('deleteHeroUser'))
    // );
  }

  /** POST: updateUser the user on the server. Returns the updated user upon success. */
  updateUser(user: User): Observable<User> {
    // httpOptions.headers =
    //   httpOptions.headers.set('Authorization', 'my-new-auth-token');

    return this.http.post<User>(this.userUrl + user.name + "/updateUser", user, httpOptions)
    // .pipe(
    //   catchError(this.handleError('updateUser', user))
    // );
  }


}



