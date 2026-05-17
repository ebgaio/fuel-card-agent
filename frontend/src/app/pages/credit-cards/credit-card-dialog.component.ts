import { Component, Inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { CreditCard } from '../../core/models/credit-card.model';
import { Customer } from '../../core/models/customer.model';
import { CustomerService } from '../../core/services/customer.service';

export interface CreditCardDialogData {
  mode: 'create' | 'edit';
  card?: CreditCard;
}

@Component({
  selector: 'app-credit-card-dialog',
  standalone: true,
  imports: [
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
  ],
  template: `
    <h2 mat-dialog-title>
      {{ data.mode === 'create' ? 'Novo Cartão' : 'Editar Cartão' }}
    </h2>
    <mat-dialog-content>
      <div class="dialog-form">
        <mat-form-field appearance="outline">
          <mat-label>Número do Cartão</mat-label>
          <input
            matInput
            [(ngModel)]="card.cardNumber"
            required
            id="input-card-number"
          />
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Titular</mat-label>
          <input
            matInput
            [(ngModel)]="card.cardHolder"
            required
            id="input-card-holder"
          />
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Cliente</mat-label>
          <mat-select
            [(ngModel)]="card.customerId"
            required
            id="select-customer"
          >
            @for (customer of customers(); track customer.id) {
              <mat-option [value]="customer.id">
                {{ customer.name }}
              </mat-option>
            }
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Tipo do Cartão</mat-label>
          <mat-select
            [(ngModel)]="card.cardType"
            required
            id="select-card-type"
          >
            <mat-option value="GAS_STATION">Posto de Gasolina</mat-option>
            <mat-option value="STANDARD_CREDIT">Crédito Padrão</mat-option>
            <mat-option value="DEBIT">Débito</mat-option>
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Limite de Crédito</mat-label>
          <input
            matInput
            [(ngModel)]="card.creditLimit"
            type="number"
            required
            id="input-credit-limit"
          />
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Data de Vencimento</mat-label>
          <input
            matInput
            [(ngModel)]="card.expirationDate"
            type="date"
            required
            id="input-expiration-date"
          />
        </mat-form-field>
      </div>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Cancelar</button>
      <button
        mat-raised-button
        color="primary"
        (click)="save()"
        [disabled]="!isValid()"
        id="btn-save-card"
      >
        {{ data.mode === 'create' ? 'Criar' : 'Salvar' }}
      </button>
    </mat-dialog-actions>
  `,
  styles: [
    `
      .dialog-form {
        display: flex;
        flex-direction: column;
        gap: 4px;
        min-width: 440px;
        padding-top: 8px;
      }
    `,
  ],
})
export class CreditCardDialogComponent implements OnInit {
  card: CreditCard;
  customers = signal<Customer[]>([]);

  constructor(
    public dialogRef: MatDialogRef<CreditCardDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: CreditCardDialogData,
    private customerService: CustomerService,
  ) {
    this.card = data.card
      ? { ...data.card }
      : {
          cardNumber: '',
          cardHolder: '',
          expirationDate: '',
          cardType: 'GAS_STATION',
          creditLimit: 0,
          customerId: 0,
        };
  }

  ngOnInit(): void {
    this.customerService.findAll().subscribe({
      next: (customers) => this.customers.set(customers),
    });
  }

  isValid(): boolean {
    return (
      !!this.card.cardNumber.trim() &&
      !!this.card.cardHolder.trim() &&
      !!this.card.cardType &&
      this.card.creditLimit > 0 &&
      this.card.customerId > 0 &&
      !!this.card.expirationDate
    );
  }

  save(): void {
    if (this.isValid()) {
      this.dialogRef.close(this.card);
    }
  }
}
