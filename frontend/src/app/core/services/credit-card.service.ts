import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  CreditCard,
  CardStats,
  CreditCardLimit,
} from '../models/credit-card.model';

@Injectable({ providedIn: 'root' })
export class CreditCardService {
  private readonly apiUrl = '/api/credit-cards';

  constructor(private http: HttpClient) {}

  findAll(): Observable<CreditCard[]> {
    return this.http.get<CreditCard[]>(this.apiUrl);
  }

  findById(id: number): Observable<CreditCard> {
    return this.http.get<CreditCard>(`${this.apiUrl}/${id}`);
  }

  findGasStationCards(): Observable<CreditCard[]> {
    return this.http.get<CreditCard[]>(`${this.apiUrl}/gas-station`);
  }

  findByCustomerId(customerId: number): Observable<CreditCard[]> {
    return this.http.get<CreditCard[]>(
      `${this.apiUrl}/customer/${customerId}`,
    );
  }

  getStats(): Observable<CardStats> {
    return this.http.get<CardStats>(`${this.apiUrl}/stats`);
  }

  findByLimitRange(
    referenceValue: number,
    percentage: number,
  ): Observable<CreditCardLimit[]> {
    return this.http.get<CreditCardLimit[]>(`${this.apiUrl}/by-limit-range`, {
      params: {
        referenceValue: referenceValue.toString(),
        percentage: percentage.toString(),
      },
    });
  }

  create(card: CreditCard): Observable<CreditCard> {
    return this.http.post<CreditCard>(this.apiUrl, card);
  }

  update(id: number, card: CreditCard): Observable<CreditCard> {
    return this.http.put<CreditCard>(`${this.apiUrl}/${id}`, card);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
