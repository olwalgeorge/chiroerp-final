# ADR-048: Frontend Architecture & Design System

**Status**: Draft (Not Implemented)
**Date**: 2026-02-03
**Deciders**: Architecture Team, Product Team, UX Team
**Priority**: P2 (Medium - User Experience Critical)
**Tier**: Core
**Tags**: frontend, ui, react, design-system, web, components

## Context
ChiroERP requires a frontend architecture that can present a large, modular ERP surface area as a coherent product experience. This ADR defines the frontend architecture, composition approach, and design system standards to support role-based work, high-volume data entry, and cross-domain navigation at SMB and enterprise scales. World-class ERPs require:
- **Responsive UI** for web, tablet, mobile
- **Design system** for consistency across 72 modules
- **Performance** (fast page loads, large data grids)
- **Accessibility** (WCAG 2.1 AA compliance)
- **Offline-first** capabilities for field service, warehouse
- **Embedded analytics** (dashboards, charts)

### Design Inputs
- Backend REST APIs provide primary UI integration points (ADR-010)
- Domain events support real-time and asynchronous UI updates where appropriate
- Domain set is broad; UI composition must scale across many modules without fragmentation
- UX must support both operational flows (fast data entry) and analytical views (dashboards/reports)

### Problem Statement
How do we build a **scalable, performant, accessible frontend** that serves SMB to enterprise customers across web, tablet, and mobile?

## Decision
Implement a **React-based micro-frontend architecture** with a centralized design system and domain-driven UI composition.

---

## Architecture Overview

### High-Level Structure

```
┌─────────────────────────────────────────────────────┐
│          ChiroERP Web Application (SPA)             │
├─────────────────────────────────────────────────────┤
│                  Shell Application                   │
│  ┌──────────────────────────────────────────────┐  │
│  │  Navigation | Header | Auth | Notifications   │  │
│  └──────────────────────────────────────────────┘  │
├─────────────────────────────────────────────────────┤
│               Micro-Frontend Modules                │
│  ┌───────┐  ┌──────┐  ┌───────┐  ┌──────────┐   │
│  │Finance│  │Sales │  │Invent.│  │Manufact. │   │
│  │  MFE  │  │ MFE  │  │  MFE  │  │   MFE    │   │
│  └───────┘  └──────┘  └───────┘  └──────────┘   │
├─────────────────────────────────────────────────────┤
│           Shared Component Library (DS)             │
│  ┌─────┐ ┌─────┐ ┌──────┐ ┌─────┐ ┌──────┐      │
│  │Table│ │Form │ │Button│ │Modal│ │Chart │      │
│  └─────┘ └─────┘ └──────┘ └─────┘ └──────┘      │
├─────────────────────────────────────────────────────┤
│              API & State Management                 │
│  ┌──────────────┐  ┌────────────┐  ┌──────────┐  │
│  │ React Query  │  │   Zustand  │  │  WebSocket│  │
│  │(Server State)│  │(Client State)│ │(Real-time)│  │
│  └──────────────┘  └────────────┘  └──────────┘  │
└─────────────────────────────────────────────────────┘
```

---

## Technology Stack

### Core Framework: React 18+

**Why React (Not Angular/Vue/Svelte)**

| Criteria | React | Angular | Vue | Justification |
|----------|-------|---------|-----|---------------|
| **Ecosystem** | ✅ Largest | ✅ Large | ⚠️ Medium | React has most ERP-grade libraries |
| **Performance** | ✅ Virtual DOM + Concurrent | ✅ AOT | ✅ Lightweight | React 18 concurrent rendering ideal for large grids |
| **TypeScript** | ✅ Excellent | ✅ Native | ✅ Good | All support, React+TS is industry standard |
| **Micro-Frontends** | ✅ Module Federation | ⚠️ Harder | ⚠️ Limited | React + Webpack MF is proven |
| **Talent Pool** | ✅ Largest | ⚠️ Smaller | ⚠️ Smaller | Easier hiring for React devs |
| **Data Grids** | ✅ AG Grid, TanStack | ✅ AG Grid | ⚠️ Limited | React has best grid libraries |
| **Design Systems** | ✅ MUI, Ant, Chakra | ⚠️ Material | ⚠️ Limited | React has most mature options |

**Decision**: **React 18+ with TypeScript**

---

### Micro-Frontend Architecture

#### Module Federation (Webpack 5)

```typescript
// finance-mfe/webpack.config.js
module.exports = {
  plugins: [
    new ModuleFederationPlugin({
      name: 'finance',
      filename: 'remoteEntry.js',
      exposes: {
        './GLModule': './src/modules/GL',
        './APModule': './src/modules/AP',
        './ARModule': './src/modules/AR',
        './AssetsModule': './src/modules/Assets',
      },
      shared: {
        react: { singleton: true, requiredVersion: '^18.0.0' },
        'react-dom': { singleton: true, requiredVersion: '^18.0.0' },
        '@chiroerp/design-system': { singleton: true },
      },
    }),
  ],
};

// shell-app/webpack.config.js
module.exports = {
  plugins: [
    new ModuleFederationPlugin({
      name: 'shell',
      remotes: {
        finance: 'finance@http://localhost:8001/remoteEntry.js',
        sales: 'sales@http://localhost:8002/remoteEntry.js',
        inventory: 'inventory@http://localhost:8003/remoteEntry.js',
        manufacturing: 'manufacturing@http://localhost:8004/remoteEntry.js',
      },
      shared: {
        react: { singleton: true },
        'react-dom': { singleton: true },
        '@chiroerp/design-system': { singleton: true },
      },
    }),
  ],
};
```

#### Lazy Loading Micro-Frontends

```tsx
// shell-app/src/App.tsx
import React, { lazy, Suspense } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { Shell } from '@chiroerp/shell';

// Lazy load micro-frontends
const FinanceModule = lazy(() => import('finance/GLModule'));
const SalesModule = lazy(() => import('sales/OrderModule'));
const InventoryModule = lazy(() => import('inventory/StockModule'));

export const App = () => {
  return (
    <BrowserRouter>
      <Shell>
        <Suspense fallback={<LoadingSpinner />}>
          <Routes>
            <Route path="/finance/*" element={<FinanceModule />} />
            <Route path="/sales/*" element={<SalesModule />} />
            <Route path="/inventory/*" element={<InventoryModule />} />
            {/* 72 modules total */}
          </Routes>
        </Suspense>
      </Shell>
    </BrowserRouter>
  );
};
```

**Benefits**:
- ✅ **Independent deployment**: Finance team deploys finance-mfe without touching sales-mfe
- ✅ **Team autonomy**: Domain teams own their UI modules
- ✅ **Bundle splitting**: Load only needed modules (SMB = Finance only, Enterprise = All 72)
- ✅ **Version isolation**: Can run Finance v2.1 + Sales v2.0 simultaneously

---

## Design System

### Component Library: Custom Built on Radix UI

**Why Custom (Not MUI/Ant Design)**

| Option | Pros | Cons | Decision |
|--------|------|------|----------|
| **Material UI** | Mature, Google-backed | Heavy bundle (1.2MB), Google aesthetic | ❌ Too opinionated |
| **Ant Design** | ERP-focused, tables/forms | Chinese design language, large bundle | ⚠️ Consider for China |
| **Chakra UI** | Accessible, themeable | Less ERP-grade components | ❌ Too consumer-focused |
| **Radix UI** | Headless, accessible | No styling (DIY) | ✅ **Best foundation** |
| **Custom + Radix** | Full control, lightweight | Build cost | ✅ **Decision** |

**Decision**: Build **@chiroerp/design-system** on top of **Radix UI primitives**

---

### Design Tokens

```typescript
// design-system/src/tokens/colors.ts
export const colors = {
  // Brand
  primary: {
    50: '#E3F2FD',
    100: '#BBDEFB',
    500: '#2196F3', // Main brand blue
    700: '#1976D2',
    900: '#0D47A1',
  },

  // Semantic
  success: {
    50: '#E8F5E9',
    500: '#4CAF50',
    700: '#388E3C',
  },
  error: {
    50: '#FFEBEE',
    500: '#F44336',
    700: '#D32F2F',
  },
  warning: {
    50: '#FFF3E0',
    500: '#FF9800',
    700: '#F57C00',
  },

  // Neutral (UI)
  gray: {
    50: '#FAFAFA',
    100: '#F5F5F5',
    200: '#EEEEEE',
    500: '#9E9E9E',
    900: '#212121',
  },
};

// design-system/src/tokens/spacing.ts
export const spacing = {
  0: '0',
  1: '0.25rem', // 4px
  2: '0.5rem',  // 8px
  3: '0.75rem', // 12px
  4: '1rem',    // 16px
  6: '1.5rem',  // 24px
  8: '2rem',    // 32px
  12: '3rem',   // 48px
};

// design-system/src/tokens/typography.ts
export const typography = {
  fonts: {
    sans: 'Inter, system-ui, sans-serif',
    mono: 'JetBrains Mono, monospace',
  },
  fontSizes: {
    xs: '0.75rem',   // 12px
    sm: '0.875rem',  // 14px
    base: '1rem',    // 16px
    lg: '1.125rem',  // 18px
    xl: '1.25rem',   // 20px
    '2xl': '1.5rem', // 24px
  },
  fontWeights: {
    normal: 400,
    medium: 500,
    semibold: 600,
    bold: 700,
  },
};
```

---

### Core Components

#### Button Component
```tsx
// design-system/src/components/Button.tsx
import React from 'react';
import { cva, type VariantProps } from 'class-variance-authority';

const buttonVariants = cva(
  'inline-flex items-center justify-center rounded-md font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 disabled:opacity-50',
  {
    variants: {
      variant: {
        primary: 'bg-primary-500 text-white hover:bg-primary-600',
        secondary: 'bg-gray-200 text-gray-900 hover:bg-gray-300',
        outline: 'border border-gray-300 bg-transparent hover:bg-gray-50',
        ghost: 'hover:bg-gray-100',
        danger: 'bg-error-500 text-white hover:bg-error-600',
      },
      size: {
        sm: 'h-8 px-3 text-sm',
        md: 'h-10 px-4 text-base',
        lg: 'h-12 px-6 text-lg',
      },
    },
    defaultVariants: {
      variant: 'primary',
      size: 'md',
    },
  }
);

export interface ButtonProps
  extends React.ButtonHTMLAttributes<HTMLButtonElement>,
    VariantProps<typeof buttonVariants> {
  isLoading?: boolean;
}

export const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant, size, isLoading, children, ...props }, ref) => {
    return (
      <button
        ref={ref}
        className={buttonVariants({ variant, size, className })}
        disabled={isLoading || props.disabled}
        {...props}
      >
        {isLoading && <Spinner className="mr-2" />}
        {children}
      </button>
    );
  }
);
```

#### Data Table Component (ERP-Grade)
```tsx
// design-system/src/components/DataTable.tsx
import React from 'react';
import {
  useReactTable,
  getCoreRowModel,
  getSortedRowModel,
  getFilteredRowModel,
  getPaginationRowModel,
  ColumnDef,
  flexRender,
} from '@tanstack/react-table';

export interface DataTableProps<TData> {
  columns: ColumnDef<TData>[];
  data: TData[];
  isLoading?: boolean;
  enablePagination?: boolean;
  enableSorting?: boolean;
  enableFiltering?: boolean;
  onRowClick?: (row: TData) => void;
}

export function DataTable<TData>({
  columns,
  data,
  isLoading,
  enablePagination = true,
  enableSorting = true,
  enableFiltering = true,
  onRowClick,
}: DataTableProps<TData>) {
  const table = useReactTable({
    data,
    columns,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: enableSorting ? getSortedRowModel() : undefined,
    getFilteredRowModel: enableFiltering ? getFilteredRowModel() : undefined,
    getPaginationRowModel: enablePagination ? getPaginationRowModel() : undefined,
  });

  if (isLoading) {
    return <TableSkeleton />;
  }

  return (
    <div className="rounded-md border">
      <table className="w-full">
        <thead className="bg-gray-50">
          {table.getHeaderGroups().map(headerGroup => (
            <tr key={headerGroup.id}>
              {headerGroup.headers.map(header => (
                <th
                  key={header.id}
                  className="px-4 py-3 text-left text-sm font-semibold text-gray-900"
                >
                  {flexRender(header.column.columnDef.header, header.getContext())}
                </th>
              ))}
            </tr>
          ))}
        </thead>
        <tbody className="divide-y divide-gray-200">
          {table.getRowModel().rows.map(row => (
            <tr
              key={row.id}
              onClick={() => onRowClick?.(row.original)}
              className="hover:bg-gray-50 cursor-pointer"
            >
              {row.getVisibleCells().map(cell => (
                <td key={cell.id} className="px-4 py-3 text-sm text-gray-900">
                  {flexRender(cell.column.columnDef.cell, cell.getContext())}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>

      {enablePagination && <TablePagination table={table} />}
    </div>
  );
}
```

#### Form Components (React Hook Form + Zod)
```tsx
// design-system/src/components/Form.tsx
import React from 'react';
import { useForm, FormProvider } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';

// Example: Sales Order Form
const salesOrderSchema = z.object({
  customerId: z.string().min(1, 'Customer is required'),
  orderDate: z.date(),
  paymentTerms: z.enum(['NET30', 'NET60', 'IMMEDIATE']),
  lineItems: z.array(z.object({
    materialId: z.string(),
    quantity: z.number().min(1),
    unitPrice: z.number().min(0),
  })).min(1, 'At least one line item required'),
});

type SalesOrderFormData = z.infer<typeof salesOrderSchema>;

export const SalesOrderForm = () => {
  const form = useForm<SalesOrderFormData>({
    resolver: zodResolver(salesOrderSchema),
    defaultValues: {
      orderDate: new Date(),
      paymentTerms: 'NET30',
      lineItems: [{ materialId: '', quantity: 1, unitPrice: 0 }],
    },
  });

  const onSubmit = async (data: SalesOrderFormData) => {
    await createSalesOrder(data);
  };

  return (
    <FormProvider {...form}>
      <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
        <FormField
          name="customerId"
          label="Customer"
          render={({ field }) => (
            <CustomerSelect {...field} />
          )}
        />

        <FormField
          name="orderDate"
          label="Order Date"
          render={({ field }) => (
            <DatePicker {...field} />
          )}
        />

        <FormField
          name="paymentTerms"
          label="Payment Terms"
          render={({ field }) => (
            <Select {...field}>
              <SelectOption value="NET30">Net 30 Days</SelectOption>
              <SelectOption value="NET60">Net 60 Days</SelectOption>
              <SelectOption value="IMMEDIATE">Immediate</SelectOption>
            </Select>
          )}
        />

        <LineItemsFieldArray name="lineItems" />

        <Button type="submit" isLoading={form.formState.isSubmitting}>
          Create Order
        </Button>
      </form>
    </FormProvider>
  );
};
```

---

## State Management

### Server State: React Query (TanStack Query)

```typescript
// hooks/useGLAccounts.ts
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { glAccountsApi } from '@/api/finance/gl-accounts';

export const useGLAccounts = (filters?: GLAccountFilters) => {
  return useQuery({
    queryKey: ['gl-accounts', filters],
    queryFn: () => glAccountsApi.list(filters),
    staleTime: 5 * 60 * 1000, // 5 minutes
    cacheTime: 10 * 60 * 1000, // 10 minutes
  });
};

export const useCreateGLAccount = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: glAccountsApi.create,
    onSuccess: () => {
      // Invalidate and refetch
      queryClient.invalidateQueries({ queryKey: ['gl-accounts'] });
    },
    onError: (error: ApiError) => {
      toast.error(error.message);
    },
  });
};

// Usage in component
const GLAccountList = () => {
  const { data, isLoading, error } = useGLAccounts({ accountType: 'ASSET' });
  const createMutation = useCreateGLAccount();

  if (isLoading) return <Spinner />;
  if (error) return <ErrorState error={error} />;

  return (
    <DataTable
      columns={glAccountColumns}
      data={data.accounts}
      onRowClick={account => navigate(`/finance/gl/accounts/${account.id}`)}
    />
  );
};
```

### Client State: Zustand

```typescript
// stores/useAppStore.ts
import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface AppState {
  // Current user & tenant
  currentUser: User | null;
  currentTenant: Tenant | null;
  currentCompanyCode: CompanyCode | null;

  // UI state
  sidebarOpen: boolean;
  theme: 'light' | 'dark';

  // Actions
  setCurrentUser: (user: User) => void;
  setCurrentTenant: (tenant: Tenant) => void;
  setCurrentCompanyCode: (companyCode: CompanyCode) => void;
  toggleSidebar: () => void;
  setTheme: (theme: 'light' | 'dark') => void;
}

export const useAppStore = create<AppState>()(
  persist(
    (set) => ({
      currentUser: null,
      currentTenant: null,
      currentCompanyCode: null,
      sidebarOpen: true,
      theme: 'light',

      setCurrentUser: (user) => set({ currentUser: user }),
      setCurrentTenant: (tenant) => set({ currentTenant: tenant }),
      setCurrentCompanyCode: (companyCode) => set({ currentCompanyCode: companyCode }),
      toggleSidebar: () => set((state) => ({ sidebarOpen: !state.sidebarOpen })),
      setTheme: (theme) => set({ theme }),
    }),
    {
      name: 'chiroerp-app-storage',
      partialize: (state) => ({
        theme: state.theme,
        sidebarOpen: state.sidebarOpen,
      }),
    }
  )
);
```

---

## Real-Time Updates (WebSocket)

```typescript
// hooks/useRealtimeUpdates.ts
import { useEffect } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { io } from 'socket.io-client';

export const useRealtimeUpdates = (entityType: string, entityId: string) => {
  const queryClient = useQueryClient();

  useEffect(() => {
    const socket = io('wss://api.chiroerp.com', {
      auth: { token: getAuthToken() },
    });

    socket.on('connect', () => {
      socket.emit('subscribe', { entityType, entityId });
    });

    socket.on('entity-updated', (data) => {
      // Invalidate React Query cache
      queryClient.invalidateQueries({ queryKey: [entityType, entityId] });

      // Show toast notification
      toast.info(`${entityType} ${entityId} was updated`);
    });

    return () => {
      socket.emit('unsubscribe', { entityType, entityId });
      socket.disconnect();
    };
  }, [entityType, entityId, queryClient]);
};

// Usage: Real-time sales order updates
const SalesOrderDetail = ({ orderId }: { orderId: string }) => {
  useRealtimeUpdates('sales-order', orderId);

  const { data: order } = useSalesOrder(orderId);

  return <OrderDetailView order={order} />;
};
```

---

## Offline-First Capabilities (Field Service, Warehouse)

```typescript
// service-worker.ts (Workbox)
import { precacheAndRoute } from 'workbox-precaching';
import { registerRoute } from 'workbox-routing';
import { NetworkFirst, CacheFirst, StaleWhileRevalidate } from 'workbox-strategies';
import { BackgroundSyncPlugin } from 'workbox-background-sync';

// Precache static assets
precacheAndRoute(self.__WB_MANIFEST);

// API requests: Network first, fallback to cache
registerRoute(
  ({ url }) => url.pathname.startsWith('/api/'),
  new NetworkFirst({
    cacheName: 'api-cache',
    plugins: [
      new BackgroundSyncPlugin('api-queue', {
        maxRetentionTime: 24 * 60, // 24 hours
      }),
    ],
  })
);

// Images: Cache first
registerRoute(
  ({ request }) => request.destination === 'image',
  new CacheFirst({
    cacheName: 'image-cache',
  })
);

// Background sync for offline mutations
self.addEventListener('sync', (event) => {
  if (event.tag === 'sync-offline-mutations') {
    event.waitUntil(syncOfflineMutations());
  }
});
```

```typescript
// hooks/useOfflineMutation.ts
import { useMutation } from '@tanstack/react-query';
import { useNetworkState } from '@/hooks/useNetworkState';

export const useOfflineMutation = <TData, TVariables>(
  mutationFn: (variables: TVariables) => Promise<TData>
) => {
  const { isOnline } = useNetworkState();

  return useMutation({
    mutationFn: async (variables: TVariables) => {
      if (!isOnline) {
        // Queue mutation for background sync
        await queueOfflineMutation(mutationFn.name, variables);
        return null as TData; // Optimistic UI
      }

      return mutationFn(variables);
    },
  });
};

// Usage: Warehouse stock count (works offline)
const useCreateStockCount = () => {
  return useOfflineMutation(
    (data: CreateStockCountRequest) => stockCountApi.create(data)
  );
};
```

---

## Embedded Analytics

### Chart Library: Recharts

```tsx
// components/DashboardWidget.tsx
import React from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend } from 'recharts';
import { Card } from '@chiroerp/design-system';

export const RevenueChart = () => {
  const { data } = useRevenueData();

  return (
    <Card title="Revenue Trend">
      <LineChart width={600} height={300} data={data}>
        <CartesianGrid strokeDasharray="3 3" />
        <XAxis dataKey="month" />
        <YAxis />
        <Tooltip />
        <Legend />
        <Line type="monotone" dataKey="revenue" stroke="#2196F3" strokeWidth={2} />
        <Line type="monotone" dataKey="target" stroke="#4CAF50" strokeWidth={2} strokeDasharray="5 5" />
      </LineChart>
    </Card>
  );
};
```

### Dashboard Composition
```tsx
// finance/src/pages/FinanceDashboard.tsx
export const FinanceDashboard = () => {
  return (
    <DashboardLayout>
      <DashboardGrid>
        <KPICard title="Total Revenue" value="$1.2M" trend="+12%" />
        <KPICard title="Outstanding AR" value="$340K" trend="-5%" />
        <KPICard title="Open Invoices" value="142" trend="+8%" />
        <KPICard title="Days Sales Outstanding" value="38 days" trend="-2 days" />

        <RevenueChart />
        <ARAgingChart />
        <TopCustomersTable />
        <RecentInvoicesTable />
      </DashboardGrid>
    </DashboardLayout>
  );
};
```

---

## Accessibility (WCAG 2.1 AA)

### Focus Management
```tsx
// components/Modal.tsx
import React, { useEffect, useRef } from 'react';
import { FocusTrap } from '@headlessui/react';

export const Modal = ({ isOpen, onClose, children }: ModalProps) => {
  const closeButtonRef = useRef<HTMLButtonElement>(null);

  useEffect(() => {
    if (isOpen) {
      closeButtonRef.current?.focus();
    }
  }, [isOpen]);

  return (
    <FocusTrap active={isOpen}>
      <div role="dialog" aria-modal="true" aria-labelledby="modal-title">
        <h2 id="modal-title">{/* Title */}</h2>
        {children}
        <button ref={closeButtonRef} onClick={onClose} aria-label="Close modal">
          Close
        </button>
      </div>
    </FocusTrap>
  );
};
```

### Keyboard Navigation
```tsx
// components/DataTable.tsx
export const DataTable = () => {
  const handleKeyDown = (event: React.KeyboardEvent, rowIndex: number) => {
    switch (event.key) {
      case 'ArrowDown':
        event.preventDefault();
        focusRow(rowIndex + 1);
        break;
      case 'ArrowUp':
        event.preventDefault();
        focusRow(rowIndex - 1);
        break;
      case 'Enter':
      case ' ':
        event.preventDefault();
        selectRow(rowIndex);
        break;
    }
  };

  return (
    <table role="grid">
      {rows.map((row, index) => (
        <tr
          key={row.id}
          role="row"
          tabIndex={0}
          onKeyDown={(e) => handleKeyDown(e, index)}
          aria-selected={selectedIndex === index}
        >
          {/* Cells */}
        </tr>
      ))}
    </table>
  );
};
```

### Screen Reader Support
```tsx
// All components have aria-labels
<Button aria-label="Create new sales order">
  <PlusIcon aria-hidden="true" />
  <span className="sr-only">Create new sales order</span>
</Button>

// Live regions for dynamic content
<div role="status" aria-live="polite" aria-atomic="true">
  {successMessage && <p>{successMessage}</p>}
</div>
```

---

## Performance Optimization

### Code Splitting & Lazy Loading
```tsx
// Lazy load heavy components
const AGGridTable = lazy(() => import('./AGGridTable'));
const ChartComponent = lazy(() => import('./ChartComponent'));

// Route-based code splitting
const routes = [
  {
    path: '/finance/gl',
    component: lazy(() => import('./finance/GL')),
  },
  {
    path: '/sales/orders',
    component: lazy(() => import('./sales/Orders')),
  },
];
```

### Virtual Scrolling (Large Lists)
```tsx
// components/VirtualizedTable.tsx
import { useVirtualizer } from '@tanstack/react-virtual';

export const VirtualizedTable = ({ data }: { data: any[] }) => {
  const parentRef = useRef<HTMLDivElement>(null);

  const virtualizer = useVirtualizer({
    count: data.length,
    getScrollElement: () => parentRef.current,
    estimateSize: () => 50, // Row height
    overscan: 10, // Render 10 extra rows
  });

  return (
    <div ref={parentRef} style={{ height: '600px', overflow: 'auto' }}>
      <div style={{ height: `${virtualizer.getTotalSize()}px` }}>
        {virtualizer.getVirtualItems().map((virtualRow) => (
          <div
            key={virtualRow.index}
            style={{
              position: 'absolute',
              top: 0,
              left: 0,
              width: '100%',
              height: `${virtualRow.size}px`,
              transform: `translateY(${virtualRow.start}px)`,
            }}
          >
            <TableRow data={data[virtualRow.index]} />
          </div>
        ))}
      </div>
    </div>
  );
};
```

### Image Optimization
```tsx
// Use Next.js Image component for optimization
import Image from 'next/image';

<Image
  src="/product-image.jpg"
  alt="Product"
  width={300}
  height={300}
  loading="lazy"
  placeholder="blur"
/>
```

---

## Internationalization (i18n)

```typescript
// i18n/index.ts
import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import Backend from 'i18next-http-backend';

i18n
  .use(Backend)
  .use(initReactI18next)
  .init({
    lng: 'en',
    fallbackLng: 'en',
    supportedLngs: ['en', 'de', 'es', 'fr', 'pt', 'zh', 'ja'],
    ns: ['common', 'finance', 'sales', 'inventory'],
    defaultNS: 'common',
    backend: {
      loadPath: '/locales/{{lng}}/{{ns}}.json',
    },
  });

// Usage in component
const SalesOrderForm = () => {
  const { t } = useTranslation('sales');

  return (
    <form>
      <label>{t('sales.order.customer')}</label>
      <input placeholder={t('sales.order.customerPlaceholder')} />
    </form>
  );
};

// Translation file: locales/en/sales.json
{
  "sales": {
    "order": {
      "customer": "Customer",
      "customerPlaceholder": "Select a customer"
    }
  }
}
```

---

## Mobile Responsiveness

### Responsive Design System
```tsx
// Responsive grid
<div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
  <Card>...</Card>
  <Card>...</Card>
  <Card>...</Card>
</div>

// Mobile-specific layouts
const SalesOrderDetail = () => {
  const isMobile = useMediaQuery('(max-width: 768px)');

  return isMobile ? (
    <MobileSalesOrderView />
  ) : (
    <DesktopSalesOrderView />
  );
};
```

### Touch-Friendly Components
```tsx
// Larger touch targets (44x44px minimum)
<button className="h-11 px-6 text-base">
  Create Order
</button>

// Swipe gestures for mobile
import { useSwipeable } from 'react-swipeable';

const SwipeableCard = () => {
  const handlers = useSwipeable({
    onSwipedLeft: () => archiveItem(),
    onSwipedRight: () => favoriteItem(),
  });

  return <div {...handlers}>Swipe me!</div>;
};
```

---

## Testing Strategy

### Unit Testing (Vitest + React Testing Library)
```typescript
// Button.test.tsx
import { render, screen, fireEvent } from '@testing-library/react';
import { Button } from './Button';

describe('Button', () => {
  it('renders with correct text', () => {
    render(<Button>Click me</Button>);
    expect(screen.getByText('Click me')).toBeInTheDocument();
  });

  it('calls onClick when clicked', () => {
    const handleClick = vi.fn();
    render(<Button onClick={handleClick}>Click me</Button>);
    fireEvent.click(screen.getByText('Click me'));
    expect(handleClick).toHaveBeenCalledTimes(1);
  });

  it('shows loading spinner when isLoading', () => {
    render(<Button isLoading>Click me</Button>);
    expect(screen.getByRole('button')).toBeDisabled();
  });
});
```

### Integration Testing (Playwright)
```typescript
// e2e/sales-order-creation.spec.ts
import { test, expect } from '@playwright/test';

test('create sales order', async ({ page }) => {
  await page.goto('/sales/orders/new');

  // Fill form
  await page.selectOption('[name="customerId"]', 'CUST-001');
  await page.fill('[name="orderDate"]', '2026-02-03');
  await page.click('button:text("Add Line Item")');
  await page.selectOption('[name="lineItems[0].materialId"]', 'MAT-001');
  await page.fill('[name="lineItems[0].quantity"]', '10');

  // Submit
  await page.click('button:text("Create Order")');

  // Verify success
  await expect(page.locator('.toast-success')).toContainText('Order created successfully');
  await expect(page).toHaveURL(/\/sales\/orders\/SO-/);
});
```

### Visual Regression Testing (Chromatic)
```typescript
// .storybook/main.ts
export default {
  stories: ['../src/**/*.stories.tsx'],
  addons: ['@storybook/addon-essentials', '@storybook/addon-a11y'],
};

// Button.stories.tsx
export const Primary: Story = {
  args: {
    children: 'Primary Button',
    variant: 'primary',
  },
};

export const Disabled: Story = {
  args: {
    children: 'Disabled Button',
    disabled: true,
  },
};
```

---

## Build & Deployment

### Vite Build Configuration
```typescript
// vite.config.ts
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { compression } from 'vite-plugin-compression';

export default defineConfig({
  plugins: [
    react(),
    compression({ algorithm: 'brotli' }),
  ],
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          react: ['react', 'react-dom'],
          'react-query': ['@tanstack/react-query'],
          'design-system': ['@chiroerp/design-system'],
        },
      },
    },
    chunkSizeWarningLimit: 500,
  },
  server: {
    proxy: {
      '/api': 'http://localhost:8080', // Backend API
    },
  },
});
```

### CDN Deployment (CloudFlare)
```yaml
# .github/workflows/deploy-frontend.yml
name: Deploy Frontend
on:
  push:
    branches: [main]

jobs:
  build-and-deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-node@v3
        with:
          node-version: 20

      - name: Install dependencies
        run: npm ci

      - name: Build
        run: npm run build

      - name: Deploy to CloudFlare Pages
        uses: cloudflare/pages-action@v1
        with:
          apiToken: ${{ secrets.CLOUDFLARE_API_TOKEN }}
          accountId: ${{ secrets.CLOUDFLARE_ACCOUNT_ID }}
          projectName: chiroerp
          directory: dist
```

---

## Performance Targets

### Core Web Vitals

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| **Largest Contentful Paint (LCP)** | < 2.5s | > 4s |
| **First Input Delay (FID)** | < 100ms | > 300ms |
| **Cumulative Layout Shift (CLS)** | < 0.1 | > 0.25 |
| **Time to Interactive (TTI)** | < 3.5s | > 5s |
| **First Contentful Paint (FCP)** | < 1.8s | > 3s |

### Bundle Size Targets

| Asset | Size | Compressed |
|-------|------|------------|
| Initial JS Bundle | < 200KB | < 50KB (gzip) |
| Initial CSS | < 50KB | < 10KB (gzip) |
| Total Page Load (First Visit) | < 500KB | < 150KB (gzip) |
| Subsequent Pages (Cached) | < 100KB | < 30KB (gzip) |

---

## Monitoring & Analytics

### Performance Monitoring (Sentry)
```typescript
// sentry.config.ts
import * as Sentry from '@sentry/react';

Sentry.init({
  dsn: process.env.VITE_SENTRY_DSN,
  integrations: [
    new Sentry.BrowserTracing(),
    new Sentry.Replay(),
  ],
  tracesSampleRate: 0.1,
  replaysSessionSampleRate: 0.1,
  replaysOnErrorSampleRate: 1.0,
});
```

### Analytics (PostHog)
```typescript
// analytics.ts
import posthog from 'posthog-js';

posthog.init(process.env.VITE_POSTHOG_KEY, {
  api_host: 'https://app.posthog.com',
});

// Track user actions
export const trackEvent = (eventName: string, properties?: Record<string, any>) => {
  posthog.capture(eventName, properties);
};

// Usage
trackEvent('sales_order_created', {
  orderId: 'SO-2026-001',
  totalAmount: 1500,
});
```

---

## Alternatives Considered

### 1. Angular
**Pros**: Enterprise-focused, TypeScript-native, comprehensive framework
**Cons**: Steeper learning curve, larger bundle, slower ecosystem evolution
**Decision**: Rejected—React ecosystem more mature for ERP

### 2. Vue 3
**Pros**: Lightweight, intuitive, good DX
**Cons**: Smaller ecosystem, fewer ERP-grade components, smaller talent pool
**Decision**: Rejected—React has better ERP libraries

### 3. Server-Side Rendering (Next.js)
**Pros**: SEO, faster initial load
**Cons**: ERP is authenticated app (no SEO benefit), SSR adds complexity
**Decision**: Deferred—use Vite for now, can migrate to Next.js later if needed

### 4. Native Mobile (React Native)
**Pros**: True native experience
**Cons**: Maintenance burden (3 codebases: web, iOS, Android)
**Decision**: Deferred—PWA sufficient for v1, React Native for v2 if needed

### 5. Single-Page Monolith (No Micro-Frontends)
**Pros**: Simpler deployment
**Cons**: Cannot scale dev teams, all-or-nothing deployments
**Decision**: Rejected—micro-frontends enable team autonomy

---

## Consequences

### Positive
- ✅ **Team Autonomy**: Domain teams own their UI modules
- ✅ **Independent Deployment**: Finance team can ship without blocking Sales team
- ✅ **Bundle Splitting**: SMB loads 200KB, Enterprise loads 500KB (progressive)
- ✅ **Offline-First**: Field service and warehouse work without connectivity
- ✅ **Accessible**: WCAG 2.1 AA compliant (keyboard nav, screen readers)
- ✅ **Performance**: Core Web Vitals targets met (LCP < 2.5s)

### Negative
- ❌ **Complexity**: Micro-frontends harder to debug (cross-module interactions)
- ❌ **Module Federation Overhead**: Shared dependencies can cause version conflicts
- ❌ **Build Time**: 72 micro-frontends = longer CI/CD pipelines
- ❌ **Learning Curve**: Team needs to learn React 18, TypeScript, Webpack MF

### Neutral
- Design system maintenance (ongoing)
- Component library versioning (requires discipline)
- Mobile app decision deferred (PWA vs React Native)

---

## Implementation Plan

### Phase 1: Foundation (Months 1-3)
- ✅ Design system (@chiroerp/design-system)
- ✅ Shell application (navigation, auth, layout)
- ✅ Core components (Button, Form, Table, Modal)
- ✅ React Query + Zustand setup

### Phase 2: Core Modules (Months 4-6)
- ✅ Finance UI (GL, AP, AR, Assets)
- ✅ Sales UI (Orders, Pricing, Fulfillment)
- ✅ Inventory UI (Stock, Counting, Reservation)

### Phase 3: Advanced Features (Months 7-9)
- ✅ Embedded analytics (dashboards, charts)
- ✅ Offline-first (service worker, background sync)
- ✅ Real-time updates (WebSocket integration)

### Phase 4: Remaining Modules (Months 10-18)
- ✅ Manufacturing, Procurement, Quality, Maintenance, CRM, MDM, Analytics
- ✅ Mobile-responsive layouts
- ✅ Accessibility audit + fixes

---

## References

### Related ADRs
- ADR-010: REST API Standards (backend APIs)
- ADR-044: Configuration Framework (UI for config management)
- ADR-046: Workflow Engine (task inbox UI)
- ADR-050: Notification & Inbox (to be created)

### Technology References
- [React 18 Documentation](https://react.dev/)
- [Webpack Module Federation](https://webpack.js.org/concepts/module-federation/)
- [TanStack Query](https://tanstack.com/query/)
- [Radix UI](https://www.radix-ui.com/)
- [WCAG 2.1 Guidelines](https://www.w3.org/WAI/WCAG21/quickref/)

### Industry References
- SAP Fiori Design System
- Microsoft Fluent UI
- Salesforce Lightning Design System
- Oracle VBCS (Visual Builder Cloud Service)

---

**Status**: This ADR defines **production-grade frontend architecture**. React + Micro-Frontends + Design System enables scalable, performant, accessible ERP UI.
