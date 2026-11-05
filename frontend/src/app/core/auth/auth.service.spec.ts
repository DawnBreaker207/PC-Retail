import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient, withXhr } from '@angular/common/http';
import { AuthService } from './auth.service';
import { ApiEndpoints } from '@core/api/api.endpoints';
import { AuthResponse, LoginRequest, RegisterRequest } from '@core/models/common.model';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(withXhr()), provideHttpClientTesting(), AuthService],
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should login successfully', () => {
    const credentials: LoginRequest = { email: 'test@test.com', password: '123456' };
    const mockResponse: AuthResponse = {
      accessToken: 'token123',
      refreshToken: 'refresh456',
      user: {
        id: 1,
        fullName: 'Test',
        email: 'test@test.com',
        phone: '0123456789',
        systemRole: 'CUSTOMER',
      },
    };

    service.login(credentials).subscribe((res) => {
      expect(res.accessToken).toBe('token123');
      expect(service.isAuthenticated()).toBeTrue();
      expect(service.currentUser()?.fullName).toBe('Test');
    });

    const req = httpMock.expectOne(ApiEndpoints.auth.login);
    expect(req.request.method).toBe('POST');
    req.flush(mockResponse);
  });

  it('should logout and clear user state', () => {
    service['setSession']({
      accessToken: 't',
      refreshToken: 'r',
      user: { id: 1, fullName: 'U', email: 'u@t.com', systemRole: 'CUSTOMER' },
    });
    expect(service.isAuthenticated()).toBeTrue();

    service.logout();
    expect(service.isAuthenticated()).toBeFalse();
    expect(service.currentUser()).toBeNull();
  });

  it('should register a new user', () => {
    const data: RegisterRequest = {
      fullName: 'New',
      email: 'new@t.com',
      phone: '0123456789',
      password: 'pass',
    };

    service.register(data).subscribe((res) => {
      expect(res).toBeTruthy();
    });

    const req = httpMock.expectOne(ApiEndpoints.auth.register);
    expect(req.request.method).toBe('POST');
    req.flush({ accessToken: 'test-token', refreshToken: 'test-refresh' });
  });
});
