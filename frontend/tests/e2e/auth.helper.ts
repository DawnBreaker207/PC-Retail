import { createHmac } from 'node:crypto';
import { execFileSync } from 'node:child_process';

const SECRET_B64 =
  process.env.E2E_JWT_SECRET ||
  'RGF3bkJyZWFrZXJEYXduQnJlYWtlckRhd25CcmVha2Vy';

function b64url(input: string): string {
  return Buffer.from(input)
    .toString('base64')
    .replace(/=/g, '')
    .replace(/\+/g, '-')
    .replace(/\//g, '_');
}

export function mintToken(id: number, role: 'ADMIN' | 'STAFF' | 'CUSTOMER'): string {
  const now = Math.floor(Date.now() / 1000);
  const header = b64url(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
  const payload = b64url(
    JSON.stringify({
      sub: 'e2e',
      id,
      username: 'e2e',
      email: 'e2e@t.vn',
      role,
      iat: now,
      exp: now + 3600,
    }),
  );
  const data = `${header}.${payload}`;
  const sig = createHmac('sha256', Buffer.from(SECRET_B64, 'base64'))
    .update(data)
    .digest('base64')
    .replace(/=/g, '')
    .replace(/\+/g, '-')
    .replace(/\//g, '_');
  return `${data}.${sig}`;
}

const tokenCache: Record<string, { token: string; exp: number }> = {};

function getRealToken(email: string, password: string, fallbackId: number, fallbackRole: 'ADMIN' | 'STAFF' | 'CUSTOMER'): string {
  const now = Math.floor(Date.now() / 1000);
  if (tokenCache[email] && tokenCache[email].exp > now + 30) {
    return tokenCache[email].token;
  }
  try {
    const baseUrl = process.env.E2E_BASE_URL || 'http://localhost:8080/api/v1';
    const authUrl = baseUrl.endsWith('/') ? `${baseUrl}auth/login` : `${baseUrl}/auth/login`;
    const res = execFileSync('curl.exe', [
      '-s',
      '-X',
      'POST',
      authUrl,
      '-H',
      'Content-Type: application/json',
      '-d',
      JSON.stringify({ email, password }),
    ], { encoding: 'utf-8', timeout: 8000 });
    const parsed = JSON.parse(res);
    if (parsed.accessToken) {
      tokenCache[email] = {
        token: parsed.accessToken,
        exp: now + (parsed.expiresIn || 900),
      };
      return parsed.accessToken;
    }
  } catch (err) {
    // fallback to mintToken if backend unreachable
  }
  return mintToken(fallbackId, fallbackRole);
}

export const ADMIN = () => getRealToken('admin@pc-retail.com', 'AdminPassword123!', 1, 'ADMIN');
export const CUSTOMER = () => getRealToken('customer@pc-retail.com', 'CustomerPassword123!', 2, 'CUSTOMER');
export const STAFF = () => getRealToken('staff@pc-retail.com', 'StaffPassword123!', 3, 'STAFF');
export const auth = (token: string) => ({ Authorization: `Bearer ${token}` });
export const uuid = () =>
  'xxxxxxxx-xxxx-4xxx'.replace(/x/g, () => Math.floor(Math.random() * 16).toString(16));

// SĐT duy nhất mỗi lần gọi (tránh rate limit / lịch sử bùng lẫn nhau giữa các test)
export const guestPhone = () =>
  `09${String(10000000 + Math.floor(Math.random() * 89999999))}`;

// Điệu nhảy OTP guest đầy đủ: challenge -> request (lấy debugCode) -> verify
export async function guestOtpDance(
  request: import('@playwright/test').APIRequestContext,
  phone: string,
): Promise<void> {
  const ch = await request.get('auth/otp/challenge');
  if (ch.status() !== 200) throw new Error(`challenge failed: ${ch.status()}`);
  const { challengeId, question } = await ch.json().then((b: { data: { challengeId: string; question: string } }) => b.data);
  const m = question.match(/(\d+)\s*\+\s*(\d+)/);
  if (!m) throw new Error(`cannot parse challenge: ${question}`);
  const answer = String(Number(m[1]) + Number(m[2]));
  const req = await request.post('auth/otp/request', {
    data: { phone, challengeId, challengeAnswer: answer },
  });
  if (req.status() !== 200) throw new Error(`otp request failed: ${req.status()} ${await req.text()}`);
  const body = await req.json();
  const code = body?.data?.debugCode as string | undefined;
  if (!code) throw new Error('OTP_DEBUG off? no debugCode (xem docker logs SMS)');
  const ver = await request.post('auth/otp/verify', { data: { phone, code } });
  if (ver.status() !== 200) throw new Error(`otp verify failed: ${ver.status()}`);
}

// Suite bào tồn vĩnh viễn (checkout deduct) — tự bù khi thấp để không flake 422
export async function ensureStock(
  request: import('@playwright/test').APIRequestContext,
  skuId: number,
  minAvail = 10,
  topUp = 60,
) {
  const a = auth(ADMIN());
  const inv = await request.get(`inventory?page=0&size=10&skuId=${skuId}`, { headers: a });
  if (inv.status() !== 200) return;
  const item = (await inv.json()).data?.content?.[0];
  if (item && item.availableQuantity < minAvail) {
    await request.put(`inventory/${skuId}/restock`, {
      data: { quantity: topUp, unitCost: 100000 },
      headers: a,
    });
  }
}
