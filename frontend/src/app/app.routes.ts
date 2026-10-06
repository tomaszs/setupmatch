import { Routes } from '@angular/router';
import { ShellComponent } from './layout/shell/shell.component';
import { InventoryComponent } from './inventory/inventory.component';
import { AllocationListComponent } from './allocations/allocation-list/allocation-list.component';
import { AllocationNewComponent } from './allocations/allocation-new/allocation-new.component';
import { AllocationDetailComponent } from './allocations/allocation-detail/allocation-detail.component';

export const routes: Routes = [
  {
    path: '',
    component: ShellComponent,
    children: [
      { path: '', redirectTo: 'inventory', pathMatch: 'full' },
      { path: 'inventory', component: InventoryComponent },
      { path: 'allocations', component: AllocationListComponent },
      { path: 'allocations/new', component: AllocationNewComponent },
      { path: 'allocations/:id', component: AllocationDetailComponent },
    ],
  },
  { path: '**', redirectTo: 'inventory' },
];
