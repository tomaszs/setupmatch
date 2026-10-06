import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  AllocationDetail,
  AllocationFilters,
  AllocationSummary,
  CreateAllocationRequest,
} from '../models/allocation.model';

@Injectable({ providedIn: 'root' })
export class AllocationApiService {
  private readonly baseUrl = `${environment.apiUrl}/allocations`;

  constructor(private readonly http: HttpClient) {}

  list(filters: AllocationFilters = {}): Observable<AllocationSummary[]> {
    let params = new HttpParams();

    if (filters.state) {
      params = params.set('state', filters.state);
    }

    if (filters.employee_id) {
      params = params.set('employee_id', filters.employee_id);
    }

    return this.http.get<AllocationSummary[]>(this.baseUrl, { params });
  }

  get(id: string): Observable<AllocationDetail> {
    return this.http.get<AllocationDetail>(`${this.baseUrl}/${id}`);
  }

  create(request: CreateAllocationRequest): Observable<AllocationDetail> {
    return this.http.post<AllocationDetail>(this.baseUrl, request);
  }

  confirm(id: string): Observable<AllocationDetail> {
    return this.http.post<AllocationDetail>(`${this.baseUrl}/${id}/confirm`, {});
  }

  cancel(id: string): Observable<AllocationDetail> {
    return this.http.post<AllocationDetail>(`${this.baseUrl}/${id}/cancel`, {});
  }
}
