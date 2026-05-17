import { Component, Inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { Customer } from '../../core/models/customer.model';

export interface CustomerDialogData {
  mode: 'create' | 'edit';
  customer?: Customer;
}

@Component({
  selector: 'app-customer-dialog',
  standalone: true,
  imports: [
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
  ],
  template: `
    <h2 mat-dialog-title>
      {{ data.mode === 'create' ? 'Novo Cliente' : 'Editar Cliente' }}
    </h2>
    <mat-dialog-content>
      <div class="dialog-form">
        <mat-form-field appearance="outline">
          <mat-label>Nome</mat-label>
          <input matInput [(ngModel)]="customer.name" required id="input-name" />
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Email</mat-label>
          <input
            matInput
            [(ngModel)]="customer.email"
            type="email"
            required
            id="input-email"
          />
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Telefone</mat-label>
          <input matInput [(ngModel)]="customer.phone" id="input-phone" />
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>CPF</mat-label>
          <input matInput [(ngModel)]="customer.cpf" required id="input-cpf" />
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
        id="btn-save-customer"
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
        min-width: 400px;
        padding-top: 8px;
      }
    `,
  ],
})
export class CustomerDialogComponent {
  customer: Customer;

  constructor(
    public dialogRef: MatDialogRef<CustomerDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: CustomerDialogData,
  ) {
    this.customer = data.customer
      ? { ...data.customer }
      : { name: '', email: '', phone: '', cpf: '' };
  }

  isValid(): boolean {
    return (
      !!this.customer.name.trim() &&
      !!this.customer.email.trim() &&
      !!this.customer.cpf.trim()
    );
  }

  save(): void {
    if (this.isValid()) {
      this.dialogRef.close(this.customer);
    }
  }
}
