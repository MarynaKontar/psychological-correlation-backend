import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import {LoginModule} from "./login/login.module";
import {UserModule} from "./user/user.module";
import {UserAnswersModule} from "./user-answers/user-answers.module";
import {UserMatchModule} from "./user-match/user-match.module";

import {FormsModule} from "@angular/forms";
import {HttpClientModule} from "@angular/common/http";

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import {HeaderComponent} from "./header/header.component";
import {FooterComponent} from "./footer/footer.component";
import {HomeComponent} from "./home/home.component";

@NgModule({
  declarations: [
    AppComponent,
    HeaderComponent,
    FooterComponent,
    HomeComponent

  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    LoginModule,
    UserModule,
    UserAnswersModule,
    UserMatchModule,
    FormsModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
