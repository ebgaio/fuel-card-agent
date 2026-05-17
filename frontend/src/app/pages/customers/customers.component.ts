import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatChipsModule } from '@angular/material/chips';
import { DecimalPipe } from '@angular/common';
import { CustomerService } from '../../core/services/customer.service';
import { CreditCardService } from '../../core/services/credit-card.service';
import { Customer } from '../../core/models/customer.model';
import { CreditCard } from '../../core/models/credit-card.model';
import { CustomerDialogComponent } from './customer-dialog.component';

@Component({
  selector: 'app-customers',
  standalone: true,
  imports: [
    FormsModule,
    MatTableModule,
    MatCardModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatDialogModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatTooltipModule,
    MatChipsModule,
    DecimalPipe,
  ],
  templateUrl: './customers.component.html',
  styleUrl: './customers.component.scss',
})
export class CustomersComponent implements OnInit {
  customers = signal<Customer[]>([]);
  isLoading = signal(true);
  searchQuery = '';
  selectedCustomer = signal<Customer | null>(null);
  customerCards = signal<CreditCard[]>([]);
  displayedColumns = ['name', 'email', 'phone', 'cpf', 'actions'];

  constructor(
    private customerService: CustomerService,
    private creditCardService: CreditCardService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
  ) {}

  ngOnInit(): void {
    this.loadCustomers();
  }

  loadCustomers(): void {
    this.isLoading.set(true);
    this.customerService.findAll().subscribe({
      next: (customers) => {
        this.customers.set(customers);
        this.isLoading.set(false);
      },
      error: () => {
        this.snackBar.open('Erro ao carregar clientes', 'Fechar', {
          duration: 3000,
        });
        this.isLoading.set(false);
      },
    });
  }

  searchCustomers(): void {
    if (!this.searchQuery.trim()) {
      this.loadCustomers();
      return;
    }
    this.isLoading.set(true);
    this.customerService.searchByName(this.searchQuery).subscribe({
      next: (customers) => {
        this.customers.set(customers);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false),
    });
  }

  onSearchKeyDown(event: KeyboardEvent): void {
    if (event.key === 'Enter') {
      this.searchCustomers();
    }
  }

  selectCustomer(customer: Customer): void {
    this.selectedCustomer.set(customer);
    if (customer.id) {
      this.creditCardService.findByCustomerId(customer.id).subscribe({
        next: (cards) => this.customerCards.set(cards),
      });
    }
  }

  closeDetails(): void {
    this.selectedCustomer.set(null);
    this.customerCards.set([]);
  }

  openCreateDialog(): void {
    const dialogRef = this.dialog.open(CustomerDialogComponent, {
      width: '480px',
      data: { mode: 'create' },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.customerService.create(result).subscribe({
          next: () => {
            this.snackBar.open('Cliente criado com sucesso!', 'OK', {
              duration: 3000,
            });
            this.loadCustomers();
          },
          error: () =>
            this.snackBar.open('Erro ao criar cliente', 'Fechar', {
              duration: 3000,
            }),
        });
      }
    });
  }

  openEditDialog(customer: Customer): void {
    const dialogRef = this.dialog.open(CustomerDialogComponent, {
      width: '480px',
      data: { mode: 'edit', customer },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result && customer.id) {
        this.customerService.update(customer.id, result).subscribe({
          next: () => {
            this.snackBar.open('Cliente atualizado!', 'OK', {
              duration: 3000,
            });
            this.loadCustomers();
            this.closeDetails();
          },
          error: () =>
            this.snackBar.open('Erro ao atualizar cliente', 'Fechar', {
              duration: 3000,
            }),
        });
      }
    });
  }

  deleteCustomer(customer: Customer): void {
    if (
      customer.id &&
      confirm(`Deseja realmente excluir o cliente "${customer.name}"?`)
    ) {
      this.customerService.delete(customer.id).subscribe({
        next: () => {
          this.snackBar.open('Cliente excluído!', 'OK', { duration: 3000 });
          this.loadCustomers();
          this.closeDetails();
        },
        error: () =>
          this.snackBar.open('Erro ao excluir cliente', 'Fechar', {
            duration: 3000,
          }),
      });
    }
  }
}
