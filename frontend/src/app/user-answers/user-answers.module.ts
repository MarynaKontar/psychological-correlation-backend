import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserTestComponent } from './user-test/user-test.component';

@NgModule({
  imports: [
    CommonModule
  ],
  declarations: [UserTestComponent],
  exports: [UserTestComponent]
})
export class UserAnswersModule { }
