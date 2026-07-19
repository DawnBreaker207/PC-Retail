export const MOCK_BUILDER_SLOTS = [
  { slotType: 'CPU', slotName: 'CPU', isRemovable: false, isRequired: true },
  { slotType: 'Mainboard', slotName: 'Mainboard', isRemovable: false, isRequired: true },
  { slotType: 'RAM', slotName: 'RAM', isRemovable: false, isRequired: true },
  { slotType: 'GPU', slotName: 'VGA', isRemovable: false, isRequired: true },
  { slotType: 'PSU', slotName: 'Nguồn', isRemovable: false, isRequired: true },
  { slotType: 'Case', slotName: 'Case/Vỏ', isRemovable: false, isRequired: true },
  { slotType: 'Storage', slotName: 'Ổ cứng', isRemovable: false, isRequired: true },
  { slotType: 'Cooler', slotName: 'Tản nhiệt', isRemovable: true, isRequired: false },
  { slotType: 'Monitor', slotName: 'Màn hình', isRemovable: true, isRequired: false },
  { slotType: 'Keyboard', slotName: 'Bàn phím', isRemovable: true, isRequired: false },
  { slotType: 'Mouse', slotName: 'Chuột', isRemovable: true, isRequired: false },
  { slotType: 'Headphone', slotName: 'Tai nghe', isRemovable: true, isRequired: false },
];

export const MOCK_BUILDER_TEMPLATES = [
  {
    id: 1, name: 'PC Gaming Tầm Trung', totalPrice: 25990000, tags: ['Gaming'],
    slots: { CPU: 'Intel i5-13400F', VGA: 'RTX 4060', RAM: '16GB DDR5' },
  },
  {
    id: 2, name: 'PC Gaming Cao Cấp', totalPrice: 45990000, tags: ['Gaming', 'Cao cấp'],
    slots: { CPU: 'Intel i7-14700K', VGA: 'RTX 4070 Ti', RAM: '32GB DDR5' },
  },
  {
    id: 3, name: 'PC Workstation Đồ Họa', totalPrice: 65990000, tags: ['Đồ họa', 'Workstation'],
    slots: { CPU: 'AMD R9-7950X', VGA: 'RTX 4080', RAM: '64GB DDR5' },
  },
  {
    id: 4, name: 'PC Văn Phòng', totalPrice: 12990000, tags: ['Văn phòng'],
    slots: { CPU: 'Intel i3-12100', VGA: 'Intel UHD', RAM: '8GB DDR4' },
  },
];
