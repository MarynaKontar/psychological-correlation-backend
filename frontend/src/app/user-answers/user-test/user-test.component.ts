import { Component, OnInit } from '@angular/core';
import {TestService} from "../test.service";
import {User} from "../../profile/user/user";
import {UserService} from "../../profile/user.service";
import {UserAnswers} from "./user-answers";


@Component({
  selector: 'app-user-test',
  templateUrl: './user-test.component.html',
  styleUrls: ['./user-test.component.scss']
})
export class UserTestComponent implements OnInit {

  userAnswers: UserAnswers;

  constructor(public testService: TestService) {

  }

  ngOnInit() {
    this.testService.getTestList()
      .subscribe(res => {
        console.log(res);
        this.userAnswers = res;

      });
  }

  // getTestList(){
  //   this.testService.getTestList()
  //     .subscribe(res => this.userAnswers = res);
  // }

}
