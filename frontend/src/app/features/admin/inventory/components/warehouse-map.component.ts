import { Component, signal, computed, inject, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DragDropModule, CdkDragDrop } from '@angular/cdk/drag-drop';
import { HlmButtonDirective } from '@shared/ui/button';
import { HlmInputDirective } from '@shared/ui/input';
import { HlmBadgeDirective } from '@shared/ui/badge';
import {
  HlmDialogOverlayDirective,
  HlmDialogContentDirective,
  HlmDialogHeaderDirective,
  HlmDialogTitleDirective,
  HlmDialogDescriptionDirective,
  HlmDialogFooterDirective
} from '@shared/ui/dialog';
import {
  HlmSheetOverlayDirective,
  HlmSheetContentDirective,
  HlmSheetHeaderDirective,
  HlmSheetTitleDirective,
  HlmSheetDescriptionDirective,
  HlmSheetFooterDirective
} from '@shared/ui/sheet';
import { HlmProgressComponent } from '@shared/ui/progress';
import { ToastService } from '@core/services/toast.service';
import {
  LucideAngularModule,
  Warehouse,
  Layers,
  Search,
  Move,
  DoorOpen,
  Info,
  CheckCircle2,
  AlertTriangle,
  RotateCcw,
  Sparkles,
  Package,
  Boxes,
  Barcode,
  SlidersHorizontal,
  X,
  ExternalLink,
  Settings2,
  Plus,
  Minus,
  Undo2,
  ArrowLeft,
  GripVertical,
  ChevronRight,
  ChevronDown,
  Trash2,
  Clock,
  ArrowRight
} from 'lucide-angular';

export interface ProductInBin {
  skuId: number;
  name: string;
  skuCode: string;
  quantity: number;
  trackingType: 'SERIALIZED' | 'BULK';
  serials: string[];
}

export interface BinItem {
  id: string;
  zoneCode: string;
  shelfCode: string;
  binCode: string;
  fullCode: string;
  productCount: number;
  maxCapacity: number;
  isActive: boolean;
  boxCount: number;
  products: ProductInBin[];
}

export interface ShelfItem {
  shelfCode: string;
  shelfName: string;
  bins: BinItem[];
}

export interface ZoneItem {
  zoneCode: string;
  zoneName: string;
  category: string;
  description: string;
  shelves: ShelfItem[];
}

const INITIAL_ZONES: ZoneItem[] = [
  {
    zoneCode: 'A',
    zoneName: 'Khu A — Card màn hình (VGA / GPU)',
    category: 'VGA / GPU',
    description: 'Khu vực lưu trữ các dòng Card đồ họa cao cấp ASUS, MSI, Gigabyte',
    shelves: [
      {
        shelfCode: '01',
        shelfName: 'Kệ 01 (RTX 40 Series)',
        bins: [
          {
            id: 'A-01-01A',
            zoneCode: 'A',
            shelfCode: '01',
            binCode: '01A',
            fullCode: 'A-01-01A',
            productCount: 12,
            maxCapacity: 15,
            isActive: true,
            boxCount: 2,
            products: [
              {
                skuId: 101,
                name: 'ASUS ROG Strix GeForce RTX 4090 24GB GDDR6X',
                skuCode: 'VGA-ROG-4090',
                quantity: 12,
                trackingType: 'SERIALIZED',
                serials: ['SN4090-ROG-0811', 'SN4090-ROG-0812', 'SN4090-ROG-0813', 'SN4090-ROG-0814', 'SN4090-ROG-0815']
              }
            ]
          },
          {
            id: 'A-01-01B',
            zoneCode: 'A',
            shelfCode: '01',
            binCode: '01B',
            fullCode: 'A-01-01B',
            productCount: 8,
            maxCapacity: 12,
            isActive: true,
            boxCount: 1,
            products: [
              {
                skuId: 102,
                name: 'GIGABYTE GeForce RTX 4080 SUPER GAMING OC 16GB',
                skuCode: 'VGA-GIGA-4080S',
                quantity: 8,
                trackingType: 'SERIALIZED',
                serials: ['SN4080-GB-1021', 'SN4080-GB-1022', 'SN4080-GB-1023']
              }
            ]
          },
          {
            id: 'A-01-02A',
            zoneCode: 'A',
            shelfCode: '01',
            binCode: '02A',
            fullCode: 'A-01-02A',
            productCount: 15,
            maxCapacity: 15,
            isActive: true,
            boxCount: 3,
            products: [
              {
                skuId: 103,
                name: 'MSI GeForce RTX 4070 Ti SUPER 16G VENTUS 3X OC',
                skuCode: 'VGA-MSI-4070TIS',
                quantity: 15,
                trackingType: 'SERIALIZED',
                serials: ['SN4070-MSI-3301', 'SN4070-MSI-3302', 'SN4070-MSI-3303']
              }
            ]
          },
          {
            id: 'A-01-02B',
            zoneCode: 'A',
            shelfCode: '01',
            binCode: '02B',
            fullCode: 'A-01-02B',
            productCount: 18,
            maxCapacity: 20,
            isActive: true,
            boxCount: 2,
            products: [
              {
                skuId: 104,
                name: 'ASUS Dual GeForce RTX 4060 EVO OC Edition 8GB',
                skuCode: 'VGA-ASUS-4060',
                quantity: 18,
                trackingType: 'SERIALIZED',
                serials: ['SN4060-AS-991', 'SN4060-AS-992']
              }
            ]
          }
        ]
      },
      {
        shelfCode: '02',
        shelfName: 'Kệ 02 (Radeon RX & Phụ kiện)',
        bins: [
          {
            id: 'A-02-01A',
            zoneCode: 'A',
            shelfCode: '02',
            binCode: '01A',
            fullCode: 'A-02-01A',
            productCount: 4,
            maxCapacity: 10,
            isActive: true,
            boxCount: 1,
            products: [
              {
                skuId: 105,
                name: 'Sapphire NITRO+ AMD Radeon RX 7900 XTX Vapor-X 24GB',
                skuCode: 'VGA-SAP-7900XTX',
                quantity: 4,
                trackingType: 'SERIALIZED',
                serials: ['SN7900-SP-501', 'SN7900-SP-502']
              }
            ]
          },
          {
            id: 'A-02-01B',
            zoneCode: 'A',
            shelfCode: '02',
            binCode: '01B',
            fullCode: 'A-02-01B',
            productCount: 0,
            maxCapacity: 10,
            isActive: true,
            boxCount: 0,
            products: []
          },
          {
            id: 'A-02-02A',
            zoneCode: 'A',
            shelfCode: '02',
            binCode: '02A',
            fullCode: 'A-02-02A',
            productCount: 18,
            maxCapacity: 20,
            isActive: true,
            boxCount: 2,
            products: [
              {
                skuId: 106,
                name: 'ASUS Dual Radeon RX 7600 V2 OC Edition 8GB',
                skuCode: 'VGA-ASUS-7600',
                quantity: 18,
                trackingType: 'SERIALIZED',
                serials: ['SN7600-AS-11', 'SN7600-AS-12']
              }
            ]
          },
          {
            id: 'A-02-02B',
            zoneCode: 'A',
            shelfCode: '02',
            binCode: '02B',
            fullCode: 'A-02-02B',
            productCount: 45,
            maxCapacity: 50,
            isActive: true,
            boxCount: 5,
            products: [
              {
                skuId: 107,
                name: 'Giá đỡ Card màn hình chống xệ kim loại ARGB',
                skuCode: 'ACC-VGA-HOLDER',
                quantity: 45,
                trackingType: 'BULK',
                serials: []
              }
            ]
          }
        ]
      }
    ]
  },
  {
    zoneCode: 'B',
    zoneName: 'Khu B — Bo mạch chủ (Mainboard)',
    category: 'Mainboard',
    description: 'Khu vực lưu trữ các dòng bo mạch Intel Z790/B760 & AMD AM5',
    shelves: [
      {
        shelfCode: '01',
        shelfName: 'Kệ 01 (Bo mạch Intel LGA1700)',
        bins: [
          {
            id: 'B-01-01A',
            zoneCode: 'B',
            shelfCode: '01',
            binCode: '01A',
            fullCode: 'B-01-01A',
            productCount: 6,
            maxCapacity: 8,
            isActive: true,
            boxCount: 1,
            products: [
              {
                skuId: 201,
                name: 'Mainboard ASUS ROG MAXIMUS Z790 HERO',
                skuCode: 'MB-ASUS-Z790H',
                quantity: 6,
                trackingType: 'SERIALIZED',
                serials: ['SN-Z790H-01', 'SN-Z790H-02']
              }
            ]
          },
          {
            id: 'B-01-01B',
            zoneCode: 'B',
            shelfCode: '01',
            binCode: '01B',
            fullCode: 'B-01-01B',
            productCount: 14,
            maxCapacity: 15,
            isActive: true,
            boxCount: 2,
            products: [
              {
                skuId: 202,
                name: 'Mainboard MSI MAG B760M MORTAR WIFI II DDR5',
                skuCode: 'MB-MSI-B760M',
                quantity: 14,
                trackingType: 'SERIALIZED',
                serials: ['SN-B760M-11', 'SN-B760M-12']
              }
            ]
          },
          {
            id: 'B-01-02A',
            zoneCode: 'B',
            shelfCode: '01',
            binCode: '02A',
            fullCode: 'B-01-02A',
            productCount: 0,
            maxCapacity: 12,
            isActive: true,
            boxCount: 0,
            products: []
          }
        ]
      },
      {
        shelfCode: '02',
        shelfName: 'Kệ 02 (Bo mạch AMD AM5 & Linh kiện lắp)',
        bins: [
          {
            id: 'B-02-01A',
            zoneCode: 'B',
            shelfCode: '02',
            binCode: '01A',
            fullCode: 'B-02-01A',
            productCount: 9,
            maxCapacity: 10,
            isActive: true,
            boxCount: 1,
            products: [
              {
                skuId: 203,
                name: 'Mainboard GIGABYTE B650 AORUS ELITE AX ICE',
                skuCode: 'MB-GIGA-B650',
                quantity: 9,
                trackingType: 'SERIALIZED',
                serials: ['SN-B650-881', 'SN-B650-882']
              }
            ]
          },
          {
            id: 'B-02-01B',
            zoneCode: 'B',
            shelfCode: '02',
            binCode: '01B',
            fullCode: 'B-02-01B',
            productCount: 85,
            maxCapacity: 100,
            isActive: true,
            boxCount: 4,
            products: [
              {
                skuId: 204,
                name: 'Bộ ốc vít chân đồng Standoff & Chặn Main I/O Shield đa năng',
                skuCode: 'ACC-MB-SCREWS',
                quantity: 85,
                trackingType: 'BULK',
                serials: []
              }
            ]
          }
        ]
      }
    ]
  },
  {
    zoneCode: 'C',
    zoneName: 'Khu C — Bộ vi xử lý (CPU / Processor)',
    category: 'CPU',
    description: 'Khu vực bảo quản an ninh cao cho CPU Intel Core & AMD Ryzen Box',
    shelves: [
      {
        shelfCode: '01',
        shelfName: 'Kệ 01 (Intel Core 14th Gen)',
        bins: [
          {
            id: 'C-01-01A',
            zoneCode: 'C',
            shelfCode: '01',
            binCode: '01A',
            fullCode: 'C-01-01A',
            productCount: 18,
            maxCapacity: 20,
            isActive: true,
            boxCount: 3,
            products: [
              {
                skuId: 301,
                name: 'CPU Intel Core i9-14900K (36M Cache, up to 6.00 GHz)',
                skuCode: 'CPU-INTEL-14900K',
                quantity: 18,
                trackingType: 'SERIALIZED',
                serials: ['SN149K-001', 'SN149K-002', 'SN149K-003']
              }
            ]
          },
          {
            id: 'C-01-01B',
            zoneCode: 'C',
            shelfCode: '01',
            binCode: '01B',
            fullCode: 'C-01-01B',
            productCount: 25,
            maxCapacity: 25,
            isActive: true,
            boxCount: 4,
            products: [
              {
                skuId: 302,
                name: 'CPU Intel Core i7-14700K (33M Cache, up to 5.60 GHz)',
                skuCode: 'CPU-INTEL-14700K',
                quantity: 25,
                trackingType: 'SERIALIZED',
                serials: ['SN147K-101', 'SN147K-102']
              }
            ]
          },
          {
            id: 'C-01-02A',
            zoneCode: 'C',
            shelfCode: '01',
            binCode: '02A',
            fullCode: 'C-01-02A',
            productCount: 11,
            maxCapacity: 25,
            isActive: true,
            boxCount: 2,
            products: [
              {
                skuId: 303,
                name: 'CPU Intel Core i5-14600K (24M Cache, up to 5.30 GHz)',
                skuCode: 'CPU-INTEL-14600K',
                quantity: 11,
                trackingType: 'SERIALIZED',
                serials: ['SN146K-55', 'SN146K-56']
              }
            ]
          }
        ]
      },
      {
        shelfCode: '02',
        shelfName: 'Kệ 02 (AMD Ryzen 7000/8000 & Keo tản nhiệt)',
        bins: [
          {
            id: 'C-02-01A',
            zoneCode: 'C',
            shelfCode: '02',
            binCode: '01A',
            fullCode: 'C-02-01A',
            productCount: 19,
            maxCapacity: 20,
            isActive: true,
            boxCount: 3,
            products: [
              {
                skuId: 304,
                name: 'CPU AMD Ryzen 7 7800X3D (8 nhân 16 luồng, 3D V-Cache)',
                skuCode: 'CPU-AMD-7800X3D',
                quantity: 19,
                trackingType: 'SERIALIZED',
                serials: ['SN783D-88', 'SN783D-89']
              }
            ]
          },
          {
            id: 'C-02-01B',
            zoneCode: 'C',
            shelfCode: '02',
            binCode: '01B',
            fullCode: 'C-02-01B',
            productCount: 7,
            maxCapacity: 20,
            isActive: true,
            boxCount: 1,
            products: [
              {
                skuId: 305,
                name: 'CPU AMD Ryzen 5 7600X (6 nhân 12 luồng, up to 5.3 GHz)',
                skuCode: 'CPU-AMD-7600X',
                quantity: 7,
                trackingType: 'SERIALIZED',
                serials: ['SN760X-12', 'SN760X-13']
              }
            ]
          },
          {
            id: 'C-02-02A',
            zoneCode: 'C',
            shelfCode: '02',
            binCode: '02A',
            fullCode: 'C-02-02A',
            productCount: 65,
            maxCapacity: 100,
            isActive: true,
            boxCount: 5,
            products: [
              {
                skuId: 306,
                name: 'Keo tản nhiệt ARCTIC MX-6 (Tuýp 4g hiệu năng cao)',
                skuCode: 'ACC-ARCTIC-MX6',
                quantity: 65,
                trackingType: 'BULK',
                serials: []
              }
            ]
          }
        ]
      }
    ]
  },
  {
    zoneCode: 'D',
    zoneName: 'Khu D — Bộ nhớ RAM & Ổ cứng SSD',
    category: 'RAM & SSD',
    description: 'Khu vực lưu trữ linh kiện nhớ DDR4/DDR5 và SSD NVMe Gen 4/5',
    shelves: [
      {
        shelfCode: '01',
        shelfName: 'Kệ 01 (RAM DDR4 & DDR5)',
        bins: [
          {
            id: 'D-01-01A',
            zoneCode: 'D',
            shelfCode: '01',
            binCode: '01A',
            fullCode: 'D-01-01A',
            productCount: 22,
            maxCapacity: 30,
            isActive: true,
            boxCount: 2,
            products: [
              {
                skuId: 401,
                name: 'RAM Corsair Vengeance RGB DDR5 32GB (2x16GB) 6000MHz',
                skuCode: 'RAM-COR-DDR5-32',
                quantity: 22,
                trackingType: 'SERIALIZED',
                serials: ['SN-RAM-6001', 'SN-RAM-6002']
              }
            ]
          },
          {
            id: 'D-01-01B',
            zoneCode: 'D',
            shelfCode: '01',
            binCode: '01B',
            fullCode: 'D-01-01B',
            productCount: 35,
            maxCapacity: 40,
            isActive: true,
            boxCount: 3,
            products: [
              {
                skuId: 402,
                name: 'RAM Kingston FURY Beast 16GB (1x16GB) DDR4 3200MHz',
                skuCode: 'RAM-KING-DDR4-16',
                quantity: 35,
                trackingType: 'SERIALIZED',
                serials: ['SN-FURY-321', 'SN-FURY-322']
              }
            ]
          }
        ]
      },
      {
        shelfCode: '02',
        shelfName: 'Kệ 02 (SSD NVMe PCIe 4.0)',
        bins: [
          {
            id: 'D-02-01A',
            zoneCode: 'D',
            shelfCode: '02',
            binCode: '01A',
            fullCode: 'D-02-01A',
            productCount: 28,
            maxCapacity: 30,
            isActive: true,
            boxCount: 3,
            products: [
              {
                skuId: 403,
                name: 'Ổ cứng SSD Samsung 990 PRO 2TB PCIe Gen 4.0 NVMe',
                skuCode: 'SSD-SAM-990P-2T',
                quantity: 28,
                trackingType: 'SERIALIZED',
                serials: ['SN-990P-201', 'SN-990P-202']
              }
            ]
          },
          {
            id: 'D-02-01B',
            zoneCode: 'D',
            shelfCode: '02',
            binCode: '01B',
            fullCode: 'D-02-01B',
            productCount: 40,
            maxCapacity: 40,
            isActive: true,
            boxCount: 4,
            products: [
              {
                skuId: 404,
                name: 'Ổ cứng SSD Kingston NV2 1TB PCIe 4.0 NVMe M.2',
                skuCode: 'SSD-KING-NV2-1T',
                quantity: 40,
                trackingType: 'SERIALIZED',
                serials: ['SN-NV2-101', 'SN-NV2-102']
              }
            ]
          },
          {
            id: 'D-02-02A',
            zoneCode: 'D',
            shelfCode: '02',
            binCode: '02A',
            fullCode: 'D-02-02A',
            productCount: 0,
            maxCapacity: 20,
            isActive: true,
            boxCount: 0,
            products: []
          }
        ]
      }
    ]
  },
  {
    zoneCode: 'E',
    zoneName: 'Khu E — Nguồn máy tính (PSU) & Tản nhiệt',
    category: 'Nguồn & Tản nhiệt',
    description: 'Khu vực lưu trữ nguồn công suất thực và tản nhiệt nước AIO',
    shelves: [
      {
        shelfCode: '01',
        shelfName: 'Kệ 01 (Nguồn ATX 3.0 & PCIe 5.0)',
        bins: [
          {
            id: 'E-01-01A',
            zoneCode: 'E',
            shelfCode: '01',
            binCode: '01A',
            fullCode: 'E-01-01A',
            productCount: 8,
            maxCapacity: 10,
            isActive: true,
            boxCount: 1,
            products: [
              {
                skuId: 501,
                name: 'Nguồn máy tính Corsair RM850e 850W 80 Plus Gold ATX 3.0',
                skuCode: 'PSU-COR-RM850E',
                quantity: 8,
                trackingType: 'SERIALIZED',
                serials: ['SN-RM85-801', 'SN-RM85-802']
              }
            ]
          },
          {
            id: 'E-01-01B',
            zoneCode: 'E',
            shelfCode: '01',
            binCode: '01B',
            fullCode: 'E-01-01B',
            productCount: 10,
            maxCapacity: 10,
            isActive: true,
            boxCount: 2,
            products: [
              {
                skuId: 502,
                name: 'Nguồn máy tính MSI MAG A750GL PCIE5 750W 80 Plus Gold',
                skuCode: 'PSU-MSI-A750GL',
                quantity: 10,
                trackingType: 'SERIALIZED',
                serials: ['SN-A750-101', 'SN-A750-102']
              }
            ]
          }
        ]
      },
      {
        shelfCode: '02',
        shelfName: 'Kệ 02 (Tản nhiệt AIO & Phụ kiện cáp nguồn)',
        bins: [
          {
            id: 'E-02-01A',
            zoneCode: 'E',
            shelfCode: '02',
            binCode: '01A',
            fullCode: 'E-02-01A',
            productCount: 4,
            maxCapacity: 6,
            isActive: true,
            boxCount: 1,
            products: [
              {
                skuId: 503,
                name: 'Tản nhiệt nước NZXT Kraken Elite 360 RGB LCD Display',
                skuCode: 'COOL-NZXT-360',
                quantity: 4,
                trackingType: 'SERIALIZED',
                serials: ['SN-NZXT-3601', 'SN-NZXT-3602']
              }
            ]
          },
          {
            id: 'E-02-01B',
            zoneCode: 'E',
            shelfCode: '02',
            binCode: '01B',
            fullCode: 'E-02-01B',
            productCount: 30,
            maxCapacity: 50,
            isActive: true,
            boxCount: 3,
            products: [
              {
                skuId: 504,
                name: 'Bộ dây nguồn bọc lưới nối dài CableMod Classic ModMesh',
                skuCode: 'ACC-CABLEMOD-EXT',
                quantity: 30,
                trackingType: 'BULK',
                serials: []
              }
            ]
          }
        ]
      }
    ]
  }
];

@Component({
  selector: 'app-warehouse-map',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    DragDropModule,
    HlmButtonDirective,
    HlmInputDirective,
    HlmBadgeDirective,
    HlmDialogOverlayDirective,
    HlmDialogContentDirective,
    HlmDialogHeaderDirective,
    HlmDialogTitleDirective,
    HlmDialogDescriptionDirective,
    HlmDialogFooterDirective,
    HlmSheetOverlayDirective,
    HlmSheetContentDirective,
    HlmSheetHeaderDirective,
    HlmSheetTitleDirective,
    HlmSheetDescriptionDirective,
    HlmSheetFooterDirective,
    HlmProgressComponent,
    LucideAngularModule
  ],
  templateUrl: './warehouse-map.component.html'
})
export class WarehouseMapComponent implements OnDestroy {
  private toast = inject(ToastService);

  readonly Warehouse = Warehouse;
  readonly Layers = Layers;
  readonly Search = Search;
  readonly Move = Move;
  readonly DoorOpen = DoorOpen;
  readonly Info = Info;
  readonly CheckCircle2 = CheckCircle2;
  readonly AlertTriangle = AlertTriangle;
  readonly RotateCcw = RotateCcw;
  readonly Sparkles = Sparkles;
  readonly Package = Package;
  readonly Boxes = Boxes;
  readonly Barcode = Barcode;
  readonly SlidersHorizontal = SlidersHorizontal;
  readonly X = X;
  readonly ExternalLink = ExternalLink;
  readonly Settings2 = Settings2;
  readonly Plus = Plus;
  readonly Minus = Minus;
  readonly Undo2 = Undo2;
  readonly ArrowLeft = ArrowLeft;
  readonly GripVertical = GripVertical;
  readonly ChevronRight = ChevronRight;
  readonly ChevronDown = ChevronDown;
  readonly Trash2 = Trash2;
  readonly Clock = Clock;
  readonly ArrowRight = ArrowRight;

  // View & Mode State (Chuẩn D:\Code\Projects)
  viewMode = signal<'zone' | 'overview'>('zone');
  managingMode = signal<boolean>(false);
  selectedZoneCode = signal<string>('A');
  zoomedShelf = signal<{ zoneCode: string; shelfCode: string } | null>(null);

  // Filter & Search
  filterStatus = signal<'all' | 'empty' | 'stocked' | 'full'>('all');
  searchQuery = signal<string>('');

  // Core Warehouse State
  zones = signal<ZoneItem[]>(INITIAL_ZONES);

  // Drawer detail state
  drawerVisible = signal<boolean>(false);
  selectedBin = signal<BinItem | null>(null);
  editingCapacity = signal<number>(10);
  expandedProductKeys = signal<Set<string>>(new Set());

  // Relocation Modal State (With Countdown Timer 20s)
  relocateModalVisible = signal<boolean>(false);
  relocateSource = signal<BinItem | null>(null);
  relocateTarget = signal<BinItem | null>(null);
  relocateQuantity = signal<number>(1);
  relocateCountdown = signal<number>(20);
  private countdownTimer: any = null;

  ngOnDestroy(): void {
    this.clearCountdown();
  }

  // Computed metrics
  allBins = computed(() => {
    return this.zones().flatMap(z => z.shelves.flatMap(s => s.bins));
  });

  totalBinsCount = computed(() => this.allBins().length);
  emptyBinsCount = computed(() => this.allBins().filter(b => b.productCount === 0).length);
  fullBinsCount = computed(() => this.allBins().filter(b => b.productCount >= b.maxCapacity).length);
  totalProductsCount = computed(() => this.allBins().reduce((sum, b) => sum + b.productCount, 0));
  overallCapacityPercent = computed(() => {
    const totalMax = this.allBins().reduce((sum, b) => sum + b.maxCapacity, 0);
    return totalMax > 0 ? Math.round((this.totalProductsCount() / totalMax) * 100) : 0;
  });

  // Current zone in zone view
  activeZone = computed(() => {
    return this.zones().find(z => z.zoneCode === this.selectedZoneCode()) || this.zones()[0] || null;
  });

  // Current shelf in zoomed shelf view
  activeZoomedShelf = computed(() => {
    const target = this.zoomedShelf();
    if (!target) return null;
    const zone = this.zones().find(z => z.zoneCode === target.zoneCode);
    if (!zone) return null;
    const shelf = zone.shelves.find(s => s.shelfCode === target.shelfCode);
    return shelf ? { zone, shelf } : null;
  });

  // Filtered zones list
  filteredZones = computed(() => {
    if (this.viewMode() === 'zone') {
      const az = this.activeZone();
      return az ? [az] : [];
    }
    return this.zones();
  });

  // View navigation
  switchView(mode: 'zone' | 'overview') {
    this.viewMode.set(mode);
    this.zoomedShelf.set(null);
  }

  openShelfZoom(zoneCode: string, shelfCode: string) {
    this.zoomedShelf.set({ zoneCode, shelfCode });
  }

  closeShelfZoom() {
    this.zoomedShelf.set(null);
  }

  toggleManagingMode() {
    const next = !this.managingMode();
    this.managingMode.set(next);
    if (next) {
      this.toast.info('Đã bật chế độ Quản lý vị trí: Bạn có thể thêm ô, thêm kệ, dời hàng hoặc vô hiệu hóa ô.');
    } else {
      this.toast.success('Đã hoàn tất cấu hình vị trí kho.');
    }
  }

  toggleProductExpand(key: string) {
    this.expandedProductKeys.update(set => {
      const next = new Set(set);
      if (next.has(key)) next.delete(key);
      else next.add(key);
      return next;
    });
  }

  // Check if a bin matches current filters and search
  isBinVisible(bin: BinItem): boolean {
    const filter = this.filterStatus();
    if (filter === 'empty' && bin.productCount > 0) return false;
    if (filter === 'stocked' && (bin.productCount === 0 || bin.productCount >= bin.maxCapacity)) return false;
    if (filter === 'full' && bin.productCount < bin.maxCapacity) return false;
    return true;
  }

  isBinHighlighted(bin: BinItem): boolean {
    const q = this.searchQuery().trim().toLowerCase();
    if (!q) return false;
    if (bin.fullCode.toLowerCase().includes(q)) return true;
    return bin.products.some(p =>
      p.name.toLowerCase().includes(q) ||
      p.skuCode.toLowerCase().includes(q) ||
      p.serials.some(s => s.toLowerCase().includes(q))
    );
  }

  getFillLevel(bin: BinItem): 'empty' | 'low' | 'medium' | 'full' {
    if (bin.productCount === 0) return 'empty';
    const pct = bin.productCount / bin.maxCapacity;
    if (pct < 0.5) return 'low';
    if (pct < 0.9) return 'medium';
    return 'full';
  }

  getBinBgClass(bin: BinItem): string {
    if (!bin.isActive) {
      return 'bg-slate-100 dark:bg-slate-900/60 border border-red-300 dark:border-red-900/60 opacity-40 saturate-0 ring-1 ring-red-400/40 text-muted';
    }
    const level = this.getFillLevel(bin);
    switch (level) {
      case 'empty':
        return 'bg-bg/60 dark:bg-bg-dark/60 border-dashed border-border dark:border-border-dark text-muted hover:border-primary/50';
      case 'low':
        return 'bg-blue-50/90 dark:bg-blue-950/40 border border-blue-300 dark:border-blue-800 text-blue-950 dark:text-blue-100 hover:border-blue-500';
      case 'medium':
        return 'bg-amber-50/90 dark:bg-amber-950/40 border-2 border-amber-400 dark:border-amber-700 text-amber-950 dark:text-amber-100 hover:border-amber-500';
      case 'full':
        return 'bg-rose-500 border-2 border-rose-600 text-white font-semibold hover:border-rose-700 shadow-xs';
    }
  }

  getCapacityPercent(bin: BinItem): number {
    return bin.maxCapacity > 0 ? Math.min(100, Math.round((bin.productCount / bin.maxCapacity) * 100)) : 0;
  }

  openBinDetail(bin: BinItem) {
    this.selectedBin.set(bin);
    this.editingCapacity.set(bin.maxCapacity);
    this.expandedProductKeys.set(new Set());
    this.drawerVisible.set(true);
  }

  closeDrawer() {
    this.drawerVisible.set(false);
    this.selectedBin.set(null);
  }

  saveCapacity() {
    const bin = this.selectedBin();
    if (!bin) return;
    const newCap = Number(this.editingCapacity());
    if (newCap < bin.productCount) {
      this.toast.error(`Sức chứa tối đa (${newCap}) không được nhỏ hơn số lượng hiện có (${bin.productCount})!`);
      return;
    }

    this.zones.update(zones =>
      zones.map(z => ({
        ...z,
        shelves: z.shelves.map(s => ({
          ...s,
          bins: s.bins.map(b => b.id === bin.id ? { ...b, maxCapacity: newCap } : b)
        }))
      }))
    );

    this.selectedBin.update(b => b ? { ...b, maxCapacity: newCap } : null);
    this.toast.success(`Đã cập nhật sức chứa ô ${bin.fullCode} thành ${newCap} đơn vị!`);
  }

  // --- Managing Actions (From D:\Code\Projects) ---

  autoAddShelf(zoneCode: string) {
    this.zones.update(zones =>
      zones.map(z => {
        if (z.zoneCode !== zoneCode) return z;
        const currentCodes = z.shelves.map(s => parseInt(s.shelfCode, 10)).filter(n => !isNaN(n));
        const nextNum = (currentCodes.length > 0 ? Math.max(...currentCodes) : 0) + 1;
        const nextShelfCode = nextNum < 10 ? `0${nextNum}` : `${nextNum}`;

        const newShelf: ShelfItem = {
          shelfCode: nextShelfCode,
          shelfName: `Kệ ${nextShelfCode} (Mới thêm)`,
          bins: [
            {
              id: `${zoneCode}-${nextShelfCode}-01A`,
              zoneCode: zoneCode,
              shelfCode: nextShelfCode,
              binCode: '01A',
              fullCode: `${zoneCode}-${nextShelfCode}-01A`,
              productCount: 0,
              maxCapacity: 15,
              isActive: true,
              boxCount: 0,
              products: []
            },
            {
              id: `${zoneCode}-${nextShelfCode}-01B`,
              zoneCode: zoneCode,
              shelfCode: nextShelfCode,
              binCode: '01B',
              fullCode: `${zoneCode}-${nextShelfCode}-01B`,
              productCount: 0,
              maxCapacity: 15,
              isActive: true,
              boxCount: 0,
              products: []
            }
          ]
        };
        return { ...z, shelves: [...z.shelves, newShelf] };
      })
    );
    this.toast.success(`Đã thêm Kệ mới vào Phân khu ${zoneCode}!`);
  }

  autoAddBin(zoneCode: string, shelfCode: string) {
    this.zones.update(zones =>
      zones.map(z => {
        if (z.zoneCode !== zoneCode) return z;
        return {
          ...z,
          shelves: z.shelves.map(s => {
            if (s.shelfCode !== shelfCode) return s;
            // Generate next binCode
            const count = s.bins.length + 1;
            const letter = count % 2 === 1 ? 'A' : 'B';
            const row = Math.ceil(count / 2);
            const rowStr = row < 10 ? `0${row}` : `${row}`;
            const binCode = `${rowStr}${letter}`;
            const fullCode = `${zoneCode}-${shelfCode}-${binCode}`;

            const newBin: BinItem = {
              id: fullCode,
              zoneCode,
              shelfCode,
              binCode,
              fullCode,
              productCount: 0,
              maxCapacity: 15,
              isActive: true,
              boxCount: 0,
              products: []
            };
            return { ...s, bins: [...s.bins, newBin] };
          })
        };
      })
    );
    this.toast.success(`Đã thêm Ô mới vào Kệ ${shelfCode} (Khu ${zoneCode})!`);
  }

  deactivateBin(bin: BinItem) {
    if (bin.productCount > 0) {
      this.toast.warning(`Ô ${bin.fullCode} đang chứa hàng hóa, không thể vô hiệu hóa!`);
      return;
    }
    this.updateBinProperty(bin.id, { isActive: false });
    this.toast.info(`Đã ngưng hoạt động vị trí ${bin.fullCode}.`);
  }

  reactivateBin(bin: BinItem) {
    this.updateBinProperty(bin.id, { isActive: true });
    this.toast.success(`Đã kích hoạt lại vị trí ${bin.fullCode}.`);
  }

  deleteBin(bin: BinItem) {
    if (bin.productCount > 0) {
      this.toast.error(`Không thể xóa ô ${bin.fullCode} vì vẫn còn ${bin.productCount} sản phẩm.`);
      return;
    }
    this.zones.update(zones =>
      zones.map(z => ({
        ...z,
        shelves: z.shelves.map(s => ({
          ...s,
          bins: s.bins.filter(b => b.id !== bin.id)
        }))
      }))
    );
    this.toast.success(`Đã xóa ô vị trí ${bin.fullCode}.`);
  }

  handleAddZone() {
    const currentCodes = this.zones().map(z => z.zoneCode);
    const alphabet = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
    const nextChar = alphabet.split('').find(ch => !currentCodes.includes(ch)) || 'Z';

    const newZone: ZoneItem = {
      zoneCode: nextChar,
      zoneName: `Khu ${nextChar} — Thiết bị mạng & Phụ kiện lưu trữ`,
      category: 'Network & Accessories',
      description: 'Phân khu bổ sung quản lý thiết bị ngoại vi và switch router',
      shelves: [
        {
          shelfCode: '01',
          shelfName: 'Kệ 01 (Thiết bị mạng)',
          bins: [
            {
              id: `${nextChar}-01-01A`,
              zoneCode: nextChar,
              shelfCode: '01',
              binCode: '01A',
              fullCode: `${nextChar}-01-01A`,
              productCount: 0,
              maxCapacity: 20,
              isActive: true,
              boxCount: 0,
              products: []
            },
            {
              id: `${nextChar}-01-01B`,
              zoneCode: nextChar,
              shelfCode: '01',
              binCode: '01B',
              fullCode: `${nextChar}-01-01B`,
              productCount: 0,
              maxCapacity: 20,
              isActive: true,
              boxCount: 0,
              products: []
            }
          ]
        }
      ]
    };

    this.zones.update(list => [...list, newZone]);
    this.selectedZoneCode.set(nextChar);
    this.toast.success(`Đã tạo mới Phân khu ${nextChar} thành công!`);
  }

  deleteZone(zoneCode: string) {
    const zone = this.zones().find(z => z.zoneCode === zoneCode);
    if (!zone) return;
    const hasProducts = zone.shelves.some(s => s.bins.some(b => b.productCount > 0));
    if (hasProducts) {
      this.toast.error(`Không thể xóa Khu ${zoneCode} vì vẫn còn hàng tồn kho bên trong!`);
      return;
    }
    this.zones.update(list => list.filter(z => z.zoneCode !== zoneCode));
    const remaining = this.zones();
    if (remaining.length > 0) {
      this.selectedZoneCode.set(remaining[0].zoneCode);
    }
    this.toast.success(`Đã xóa Phân khu ${zoneCode}.`);
  }

  private updateBinProperty(binId: string, patch: Partial<BinItem>) {
    this.zones.update(zones =>
      zones.map(z => ({
        ...z,
        shelves: z.shelves.map(s => ({
          ...s,
          bins: s.bins.map(b => b.id === binId ? { ...b, ...patch } : b)
        }))
      }))
    );
    if (this.selectedBin()?.id === binId) {
      this.selectedBin.update(b => b ? { ...b, ...patch } : null);
    }
  }

  // --- Relocation with 20s Countdown Dialog (Chuẩn D:\Code\Projects) ---

  onDropBin(event: CdkDragDrop<any>) {
    const sourceBin = event.item.data as BinItem;
    const containerData = event.container.data;
    const targetBin = (Array.isArray(containerData) ? containerData[event.currentIndex] : containerData) as BinItem;
    if (!sourceBin || !targetBin || sourceBin.id === targetBin.id) return;
    this.openRelocateModal(sourceBin, targetBin);
  }

  openRelocateModal(source: BinItem, target?: BinItem) {
    if (source.productCount === 0) {
      this.toast.warning(`Ô ${source.fullCode} không có hàng hóa để điều chuyển.`);
      return;
    }

    if (!target) {
      // Find candidate in the same shelf or zone
      const zone = this.zones().find(z => z.zoneCode === source.zoneCode);
      const candidates = (zone?.shelves.flatMap(s => s.bins) || []).filter(
        b => b.id !== source.id && b.isActive && b.productCount < b.maxCapacity
      );
      if (candidates.length === 0) {
        this.toast.warning('Không tìm thấy ô trống khả dụng trong phân khu để điều chuyển tới.');
        return;
      }
      target = candidates[0];
    }

    if (source.zoneCode !== target.zoneCode) {
      this.toast.warning(`Lưu ý: Bạn đang điều chuyển giữa 2 Phân khu khác nhau (${source.zoneCode} ➔ ${target.zoneCode}).`);
    }

    if (target.productCount >= target.maxCapacity) {
      this.toast.error(`Ô đích ${target.fullCode} đã đạt sức chứa tối đa (${target.maxCapacity}).`);
      return;
    }

    this.closeDrawer();
    this.relocateSource.set(source);
    this.relocateTarget.set(target);
    this.relocateQuantity.set(1);
    this.relocateCountdown.set(20);
    this.relocateModalVisible.set(true);

    this.startCountdown();
  }

  private startCountdown() {
    this.clearCountdown();
    this.countdownTimer = setInterval(() => {
      const next = this.relocateCountdown() - 1;
      if (next <= 0) {
        this.clearCountdown();
        this.handleRelocateCancel();
        this.toast.info('Đã tự động hủy lệnh điều chuyển do hết thời gian chờ an toàn (20s).');
      } else {
        this.relocateCountdown.set(next);
      }
    }, 1000);
  }

  private clearCountdown() {
    if (this.countdownTimer) {
      clearInterval(this.countdownTimer);
      this.countdownTimer = null;
    }
  }

  handleRelocateCancel() {
    this.clearCountdown();
    this.relocateModalVisible.set(false);
    this.relocateSource.set(null);
    this.relocateTarget.set(null);
  }

  stepRelocateQty(delta: number) {
    const src = this.relocateSource();
    const tgt = this.relocateTarget();
    if (!src || !tgt) return;
    const maxAllowed = Math.min(src.productCount, tgt.maxCapacity - tgt.productCount);
    const nextVal = Math.max(1, Math.min(maxAllowed, this.relocateQuantity() + delta));
    this.relocateQuantity.set(nextVal);
  }

  setMaxRelocateQty() {
    const src = this.relocateSource();
    const tgt = this.relocateTarget();
    if (!src || !tgt) return;
    const maxAllowed = Math.min(src.productCount, tgt.maxCapacity - tgt.productCount);
    this.relocateQuantity.set(Math.max(1, maxAllowed));
  }

  handleRelocateConfirm() {
    const sourceBin = this.relocateSource();
    const targetBin = this.relocateTarget();
    const qty = this.relocateQuantity();

    if (!sourceBin || !targetBin || qty <= 0) {
      this.handleRelocateCancel();
      return;
    }

    const productToMove = sourceBin.products[0];
    if (!productToMove) {
      this.handleRelocateCancel();
      return;
    }

    this.zones.update(zones => {
      return zones.map(z => ({
        ...z,
        shelves: z.shelves.map(s => ({
          ...s,
          bins: s.bins.map(b => {
            if (b.id === sourceBin.id) {
              const updatedProducts = b.products
                .map((p, idx) => idx === 0 ? { ...p, quantity: p.quantity - qty } : p)
                .filter(p => p.quantity > 0);
              return {
                ...b,
                productCount: b.productCount - qty,
                products: updatedProducts
              };
            }
            if (b.id === targetBin.id) {
              const existing = b.products.find(p => p.skuId === productToMove.skuId);
              let updatedProducts = [...b.products];
              if (existing) {
                updatedProducts = updatedProducts.map(p =>
                  p.skuId === productToMove.skuId ? { ...p, quantity: p.quantity + qty } : p
                );
              } else {
                updatedProducts.push({
                  ...productToMove,
                  quantity: qty,
                  serials: productToMove.serials.slice(0, qty)
                });
              }
              return {
                ...b,
                productCount: b.productCount + qty,
                products: updatedProducts
              };
            }
            return b;
          })
        }))
      }));
    });

    this.clearCountdown();
    this.relocateModalVisible.set(false);
    this.toast.success(
      `Đã chuyển ${qty} linh kiện "${productToMove.name}" từ ${sourceBin.fullCode} sang ${targetBin.fullCode} (Ghi log kiểm toán RELOCATE thành công)!`
    );
  }

  resetLayout() {
    this.zones.set(INITIAL_ZONES);
    this.searchQuery.set('');
    this.filterStatus.set('all');
    this.selectedZoneCode.set('A');
    this.zoomedShelf.set(null);
    this.managingMode.set(false);
    this.toast.info('Đã hoàn nguyên cấu hình vị trí kho mặc định.');
  }
}
