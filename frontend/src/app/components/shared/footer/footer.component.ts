import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [RouterLink],
  template: `
    <footer>
      <div class="container">
        <a routerLink="/" class="logo-font">conduit</a>
        <span class="attribution">
          An interactive learning project from <a href="https://thinkster.io">Thinkster</a>.
          Code &amp; design licensed under MIT.
        </span>
      </div>
    </footer>
  `,
  styles: [`
    footer { background: #f3f3f3; padding: 1rem 0; margin-top: 3rem; }
    .container { display: flex; align-items: center; }
    .logo-font { color: #5cb85c; font-weight: 700; margin-right: 0.5rem; text-decoration: none; }
    .attribution { color: #bbb; font-size: 0.8rem; }
    .attribution a { color: #5cb85c; }
  `]
})
export class FooterComponent {}
