import { Component, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { CurrencyPipe } from '@angular/common';
import { CreditCardService } from '../../core/services/credit-card.service';
import { CustomerService } from '../../core/services/customer.service';
import { CardStats } from '../../core/models/credit-card.model';
import { Customer } from '../../core/models/customer.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    MatCardModule,
    MatIconModule,
    MatButtonModule,
    MatProgressSpinnerModule,

    CurrencyPipe,
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
})
export class DashboardComponent implements OnInit {
  stats = signal<CardStats | null>(null);
  totalCustomers = signal(0);
  totalCards = signal(0);
  recentCustomers = signal<Customer[]>([]);
  isLoading = signal(true);

  constructor(
    private creditCardService: CreditCardService,
    private customerService: CustomerService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.isLoading.set(true);

    this.creditCardService.getStats().subscribe({
      next: (stats) => this.stats.set(stats),
      error: () => this.stats.set(null),
    });

    this.customerService.findAll().subscribe({
      next: (customers) => {
        this.totalCustomers.set(customers.length);
        this.recentCustomers.set(customers.slice(0, 5));
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false),
    });

    this.creditCardService.findAll().subscribe({
      next: (cards) => this.totalCards.set(cards.length),
    });
  }

  goToChat(): void {
    this.router.navigate(['/chat']);
  }

  goToCustomers(): void {
    this.router.navigate(['/customers']);
  }

  goToCards(): void {
    this.router.navigate(['/credit-cards']);
  }
}
