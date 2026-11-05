import { Component, DestroyRef, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { LucideAngularModule, Plus, Edit2, Trash2, MapPin } from 'lucide-angular';
import { HlmButtonDirective } from '@shared/ui/button';
import { HlmBadgeDirective } from '@shared/ui/badge';
import { EmptyStateComponent } from '@shared/components/empty-state.component';
import { HlmSkeletonComponent } from '@shared/ui/skeleton';
import { HlmAlertDialogComponent } from '@shared/ui/alert-dialog';
import { ToastService } from '@core/services/toast.service';
import { UserService } from '@core/services/user.service';
import { Address } from '@core/models/common.model';
import { AddressFormComponent } from './address-form.component';

@Component({
  selector: 'app-address-book',
  imports: [
    LucideAngularModule,
    HlmButtonDirective,
    HlmBadgeDirective,
    EmptyStateComponent,
    HlmSkeletonComponent,
    HlmAlertDialogComponent,
    AddressFormComponent,
  ],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './address-book.component.html',
})
export class AddressBookComponent {
  readonly Plus = Plus;
  readonly Edit2 = Edit2;
  readonly Trash2 = Trash2;
  readonly MapPin = MapPin;

  private userService = inject(UserService);
  private destroyRef = inject(DestroyRef);
  private toast = inject(ToastService);

  addresses = signal<Address[]>([]);
  loading = signal(true);
  isCreating = signal(false);
  editingAddress = signal<Address | null>(null);
  deleteDialogOpen = signal(false);
  pendingDeleteId = signal<number | null>(null);

  constructor() {
    this.loadData();
  }

  loadData() {
    this.loading.set(true);
    this.userService
      .getAddresses()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (addresses) => {
          this.addresses.set(addresses);
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });
  }

  openCreateModal() {
    this.isCreating.set(true);
    this.editingAddress.set(null);
  }

  openEditModal(address: Address) {
    this.isCreating.set(false);
    this.editingAddress.set(address);
  }

  closeModal() {
    this.isCreating.set(false);
    this.editingAddress.set(null);
  }

  deleteAddress(id: number) {
    this.pendingDeleteId.set(id);
    this.deleteDialogOpen.set(true);
  }

  confirmDeleteAddress() {
    const id = this.pendingDeleteId();
    if (id == null) return;
    this.deleteDialogOpen.set(false);
    this.userService
      .deleteAddress(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.toast.success('Xóa địa chỉ thành công');
          this.loadData();
        },
        error: () => this.toast.error('Xóa địa chỉ thất bại'),
      });
  }
}
