import {
  ApplicationConfig,
  importProvidersFrom,
  LOCALE_ID,
  provideBrowserGlobalErrorListeners,
  provideZoneChangeDetection,
} from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { CommonModule, registerLocaleData } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideNzI18n } from 'ng-zorro-antd/i18n';
import { provideHttpClient, withInterceptors, withInterceptorsFromDi } from '@angular/common/http';
import { NZ_ICONS } from 'ng-zorro-antd/icon';
import vi from '@angular/common/locales/vi';
import { vi_VN } from 'ng-zorro-antd/i18n';
import {
  SearchOutline, ShoppingCartOutline, BellOutline, UserOutline,
  SunOutline, MoonOutline, LogoutOutline, FileTextOutline,
  LaptopOutline, PlayCircleOutline, DesktopOutline, ToolOutline,
  MonitorOutline, TagsOutline, PhoneOutline, MailOutline,
  EnvironmentOutline, EyeOutline, EyeInvisibleOutline,
  MenuFoldOutline, CloseOutline, FilterOutline, HeartOutline,
  PlusOutline, DeleteOutline, EditOutline, DownOutline,
  LeftOutline, RightOutline, UpOutline, LoadingOutline,
  StarOutline, StarFill, ThunderboltFill, AppstoreFill,
  CheckCircleFill, InfoCircleFill, ExclamationCircleFill,
  HomeOutline, ShopOutline, ShoppingOutline,
  TeamOutline, BarChartOutline, SettingOutline,
  DashboardOutline, InboxOutline, GiftOutline,
  PictureOutline, PlusCircleOutline,
  MinusOutline, ReloadOutline, SwapOutline,
} from '@ant-design/icons-angular/icons';
import { authInterceptor } from '@core/interceptors/auth.interceptor';
import { errorInterceptor } from '@core/interceptors/error.interceptor';
import { MockInterceptor } from '@core/interceptors/mock.interceptor';

registerLocaleData(vi);

const icons = [
  SearchOutline, ShoppingCartOutline, BellOutline, UserOutline,
  SunOutline, MoonOutline, LogoutOutline, FileTextOutline,
  LaptopOutline, PlayCircleOutline, DesktopOutline, ToolOutline,
  MonitorOutline, TagsOutline, PhoneOutline, MailOutline,
  EnvironmentOutline, EyeOutline, EyeInvisibleOutline,
  MenuFoldOutline, CloseOutline, FilterOutline, HeartOutline,
  PlusOutline, DeleteOutline, EditOutline, DownOutline,
  LeftOutline, RightOutline, UpOutline, LoadingOutline,
  StarOutline, StarFill, ThunderboltFill, AppstoreFill,
  CheckCircleFill, InfoCircleFill, ExclamationCircleFill,
  HomeOutline, ShopOutline, ShoppingOutline,
  TeamOutline, BarChartOutline, SettingOutline,
  DashboardOutline, InboxOutline, GiftOutline,
  PictureOutline, PlusCircleOutline,
  MinusOutline, ReloadOutline, SwapOutline,
];

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    importProvidersFrom(
      CommonModule,
      FormsModule,
      ReactiveFormsModule,
    ),
    { provide: LOCALE_ID, useValue: 'vi' },
    { provide: NZ_ICONS, useValue: icons },
    provideNzI18n(vi_VN),
    provideAnimationsAsync(),
    provideHttpClient(
      withInterceptors([authInterceptor, errorInterceptor]),
      withInterceptorsFromDi(),
    ),
    { provide: MockInterceptor, useClass: MockInterceptor },
  ],
};
