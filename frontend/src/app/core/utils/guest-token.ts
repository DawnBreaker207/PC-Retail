const KEY = 'pc-guest-token';

export function getGuestToken(): string {
  try {
    let token = localStorage.getItem(KEY);
    if (!token) {
      token =
        typeof crypto !== 'undefined' && 'randomUUID' in crypto
          ? crypto.randomUUID().replace(/-/g, '').substring(0, 8)
          : 'g' + Date.now().toString(36) + Math.floor(Math.random() * 1e6).toString(36);
      localStorage.setItem(KEY, token);
    }
    return token;
  } catch {
    return 'guest-' + Date.now().toString(36);
  }
}

export function clearGuestToken(): void {
  try {
    localStorage.removeItem(KEY);
  } catch {
    // ignore
  }
}
