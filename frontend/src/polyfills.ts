import 'zone.js';

// Shim cho sockjs-client (dùng biến `global` của Node, không có trên browser)
(window as unknown as Record<string, unknown>)['global'] ??= window;
