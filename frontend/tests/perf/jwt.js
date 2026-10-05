import crypto from 'k6/crypto';
import encoding from 'k6/encoding';

const SECRET_B64 =
  __ENV.E2E_JWT_SECRET || 'RGF3bkJyZWFrZXJEYXduQnJlYWtlckRhd25CcmVha2Vy';

function b64url(data) {
  return encoding
    .b64encode(data)
    .replace(/\+/g, '-')
    .replace(/\//g, '_')
    .replace(/=+$/g, '');
}

export function mintToken(id, role) {
  const now = Math.floor(Date.now() / 1000);
  const header = b64url(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
  const payload = b64url(
    JSON.stringify({
      sub: 'k6',
      id,
      username: 'k6',
      email: 'k6@t.vn',
      role,
      iat: now,
      exp: now + 3600,
    }),
  );
  const data = `${header}.${payload}`;
  const secret = encoding.b64decode(SECRET_B64, 'std', 's');
  // crypto.hmac đã trả base64 chuẩn -> chỉ đổi sang base64url, KHÔNG encode lại
  const sig = crypto.hmac('sha256', secret, data, 'base64');
  const sigUrl = sig.replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/g, '');
  return `${data}.${sigUrl}`;
}

export const ADMIN = () => mintToken(1, 'ADMIN');
export const CUSTOMER = () => mintToken(2, 'CUSTOMER');

export function uuid() {
  return 'xxxxxxxx-xxxx-4xxx'.replace(/x/g, () =>
    Math.floor(Math.random() * 16).toString(16),
  );
}
