import { Component, OnInit } from '@angular/core';
import {UserService} from "../user.service";
import {User} from "./user";

@Component({
  selector: 'app-user',
  templateUrl: './user.component.html',
  styleUrls: ['./user.component.scss']
})
export class UserComponent implements OnInit {

  users: Array<any>;
  user: User;
  registeredUser={};

  constructor(public userService: UserService) { }

  ngOnInit() {

    this.userService.getUsers().subscribe(res => this.users = res
      //   error => console.log(error)
    );
  }

  registerUser(){
    this.userService.add(<User>this.registeredUser)
      .subscribe(res => this.registeredUser === res);
  }

}
