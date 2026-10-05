import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiEndpoints } from '@core/api/api.endpoints';
import { Address, User } from '@core/models/common.model';

export interface UpdateProfileRequest {
  fullName: string;
  phone: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

@Injectable({ providedIn: 'root' })
export class UserService {
  private http = inject(HttpClient);

  getMe(): Observable<User> {
    return this.http.get<User>(ApiEndpoints.user.me);
  }

  updateMe(data: UpdateProfileRequest): Observable<User> {
    return this.http.put<User>(ApiEndpoints.user.me, data);
  }

  changePassword(data: ChangePasswordRequest): Observable<void> {
    return this.http.post<void>(ApiEndpoints.auth.resetPassword, data);
  }

  getAddresses(): Observable<Address[]> {
    return this.http.get<Address[]>(ApiEndpoints.user.addresses);
  }

  addAddress(data: Omit<Address, 'id'>): Observable<Address> {
    return this.http.post<Address>(ApiEndpoints.user.addresses, data);
  }

  updateAddress(id: number, data: Omit<Address, 'id'>): Observable<Address> {
    return this.http.put<Address>(ApiEndpoints.user.address(id), data);
  }

  deleteAddress(id: number): Observable<void> {
    return this.http.delete<void>(ApiEndpoints.user.address(id));
  }
}
