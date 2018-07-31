import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserTestComponent } from './user-test/user-test.component';
import {TestService} from "./test.service";
import {FormsModule} from "@angular/forms";

@NgModule({
  imports: [
    CommonModule,
    FormsModule
  ],
  declarations: [UserTestComponent],
  exports: [UserTestComponent],
  providers:[TestService]
})
export class UserAnswersModule { }
