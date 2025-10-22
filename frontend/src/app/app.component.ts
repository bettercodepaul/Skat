import { Component, OnInit } from '@angular/core';
import { HelloService } from './services/hello.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  title = 'skat-frontend';
  message: string = '';

  constructor(private helloService: HelloService) { }

  ngOnInit(): void {
    this.helloService.getHello().subscribe({
      next: (data) => {
        this.message = data;
      },
      error: (error) => {
        console.error('Error fetching hello message:', error);
        this.message = 'Error loading message';
      }
    });
  }
}
