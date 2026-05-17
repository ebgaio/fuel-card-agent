export interface CreditCard {
  id?: number;
  cardNumber: string;
  cardHolder: string;
  expirationDate: string;
  cardType: string;
  creditLimit: number;
  customerId: number;
  customerName?: string;
}

export interface CardStats {
  totalGasStationCards: number;
  averageCreditLimit: number;
  totalCreditLimit: number;
}

export interface CreditCardLimit {
  id: number;
  customerName: string;
  expirationDate: string;
  creditLimit: number;
}
