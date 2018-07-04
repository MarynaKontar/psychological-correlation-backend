import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import {UserComponent} from "./user/user/user.component";
import {UserTestComponent} from "./user-answers/user-test/user-test.component";
import {HomeComponent} from "./home/home.component";

const routes: Routes = [

  {
    path: '',
    component: HomeComponent,
    // children: [
    //   { path: '', component: HomeComponent, pathMatch: 'full'},
    //   { path: 'about', component: AboutComponent },
    //   { path: 'test/:id', component: AboutComponent }
    // ]
  },


  {
    path: 'register',
    component: UserComponent
  },

  {
    path: 'user-test',
    component: UserTestComponent
  },

  {
    path: 'home',
    component: HomeComponent
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
