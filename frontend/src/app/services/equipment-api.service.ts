import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  CreateEquipmentRequest,
  Equipment,
  EquipmentFilters,
  RetireEquipmentRequest,
} from '../models/equipment.model';

@Injectable({ providedIn: 'root' })
export class EquipmentApiService {
  private readonly baseUrl = `${environment.apiUrl}/equipments`;

  constructor(private readonly http: HttpClient) {}

  list(filters: EquipmentFilters = {}): Observable<Equipment[]> {
    let params = new HttpParams();

    if (filters.state) {
      params = params.set('state', filters.state);
    }

    if (filters.type) {
      params = params.set('type', filters.type);
    }

    if (filters.include_retired) {
      params = params.set('include_retired', 'true');
    }

    return this.http.get<Equipment[]>(this.baseUrl, { params });
  }

  create(request: CreateEquipmentRequest): Observable<Equipment> {
    return this.http.post<Equipment>(this.baseUrl, request);
  }

  retire(id: string, request: RetireEquipmentRequest): Observable<Equipment> {
    return this.http.post<Equipment>(`${this.baseUrl}/${id}/retire`, request);
  }
}
