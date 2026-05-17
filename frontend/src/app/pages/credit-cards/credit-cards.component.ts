import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatChipsModule } from '@angular/material/chips';
import { CurrencyPipe } from '@angular/common';
import { CreditCardService } from '../../core/services/credit-card.service';
import { CreditCard, CardStats } from '../../core/models/credit-card.model';
import { CreditCardDialogComponent } from './credit-card-dialog.component';

@Component({
  selector: 'app-credit-cards',
  standalone: true,
  imports: [
    FormsModule,
    MatTableModule,
    MatCardModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatSelectModule,
    MatDialogModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatTooltipModule,
    MatChipsModule,
    CurrencyPipe,

  ],
  templateUrl: './credit-cards.component.html',
  styleUrl: './credit-cards.component.scss',
})
export class CreditCardsComponent implements OnInit {
  cards = signal<CreditCard[]>([]);
  stats = signal<CardStats | null>(null);
  isLoading = signal(true);
  filterType = 'ALL';
  displayedColumns = [
    'cardNumber',
    'cardHolder',
    'customerName',
    'cardType',
    'creditLimit',
    'expirationDate',
    'actions',
  ];

  constructor(
    private creditCardService: CreditCardService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
  ) {}

  ngOnInit(): void {
    this.loadCards();
    this.loadStats();
  }

  loadCards(): void {
    this.isLoading.set(true);
    const obs =
      this.filterType === 'GAS_STATION'
        ? this.creditCardService.findGasStationCards()
        : this.creditCardService.findAll();

    obs.subscribe({
      next: (cards) => {
        this.cards.set(cards);
        this.isLoading.set(false);
      },
      error: () => {
        this.snackBar.open('Erro ao carregar cartões', 'Fechar', {
          duration: 3000,
        });
        this.isLoading.set(false);
      },
    });
  }

  loadStats(): void {
    this.creditCardService.getStats().subscribe({
      next: (stats) => this.stats.set(stats),
    });
  }

  onFilterChange(): void {
    this.loadCards();
  }

  getCardTypeLabel(type: string): string {
    const labels: Record<string, string> = {
      GAS_STATION: 'Posto',
      STANDARD_CREDIT: 'Crédito',
      DEBIT: 'Débito',
    };
    return labels[type] || type;
  }

  getCardTypeClass(type: string): string {
    const classes: Record<string, string> = {
      GAS_STATION: 'type-gas',
      STANDARD_CREDIT: 'type-credit',
      DEBIT: 'type-debit',
    };
    return classes[type] || '';
  }

  openCreateDialog(): void {
    const dialogRef = this.dialog.open(CreditCardDialogComponent, {
      width: '520px',
      data: { mode: 'create' },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.creditCardService.create(result).subscribe({
          next: () => {
            this.snackBar.open('Cartão criado com sucesso!', 'OK', {
              duration: 3000,
            });
            this.loadCards();
            this.loadStats();
          },
          error: (err) =>
            this.snackBar.open(
              'Erro ao criar cartão: ' + (err.error?.message || ''),
              'Fechar',
              { duration: 4000 },
            ),
        });
      }
    });
  }

  openEditDialog(card: CreditCard): void {
    const dialogRef = this.dialog.open(CreditCardDialogComponent, {
      width: '520px',
      data: { mode: 'edit', card },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result && card.id) {
        this.creditCardService.update(card.id, result).subscribe({
          next: () => {
            this.snackBar.open('Cartão atualizado!', 'OK', { duration: 3000 });
            this.loadCards();
            this.loadStats();
          },
          error: () =>
            this.snackBar.open('Erro ao atualizar cartão', 'Fechar', {
              duration: 3000,
            }),
        });
      }
    });
  }

  deleteCard(card: CreditCard): void {
    if (
      card.id &&
      confirm(`Deseja excluir o cartão ${card.cardNumber}?`)
    ) {
      this.creditCardService.delete(card.id).subscribe({
        next: () => {
          this.snackBar.open('Cartão excluído!', 'OK', { duration: 3000 });
          this.loadCards();
          this.loadStats();
        },
        error: () =>
          this.snackBar.open('Erro ao excluir cartão', 'Fechar', {
            duration: 3000,
          }),
      });
    }
  }
}
