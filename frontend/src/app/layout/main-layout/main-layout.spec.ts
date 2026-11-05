import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient, withXhr } from '@angular/common/http';
import { MainLayout } from './main-layout';

describe('MainLayout', () => {
  let component: MainLayout;
  let fixture: ComponentFixture<MainLayout>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MainLayout],
      providers: [
        provideRouter([]),
        provideHttpClient(withXhr()),
        provideHttpClientTesting(),
        provideNoopAnimations(),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(MainLayout);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render header, mega-menu, and footer', () => {
    const el = fixture.nativeElement as HTMLElement;
    expect(el.querySelector('app-header')).toBeTruthy();
    expect(el.querySelector('app-mega-menu')).toBeTruthy();
    expect(el.querySelector('app-footer')).toBeTruthy();
  });
});
