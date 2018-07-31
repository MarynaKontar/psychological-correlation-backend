import { Component, OnInit } from '@angular/core';
import {UserService} from "../profile/user.service";
import {User} from "../profile/user/user";

@Component({
  selector: 'app-registration',
  templateUrl: './registration.component.html',
  styleUrls: ['./registration.component.scss']
})
export class RegistrationComponent implements OnInit {

  users: Array<User>;
  registeredUser={};

  constructor(public userService: UserService) { }

  ngOnInit() {
  this.userService.getUsers().subscribe(res => this.users === res);

  }

  registerUser(){
    this.userService.add(<User> this.registeredUser)
      .subscribe(res => this.registeredUser === res);
  }

}
