# Sinh cặp khóa RS256 và ghi JWT_* vào infra/.env (giữ nguyên các biến khác).
# Chạy: pwsh infra/generate-jwt-keys.ps1
# Lưu ý: chạy lại sẽ tạo khóa mới -> toàn bộ access/refresh token cũ mất hiệu lực.
$ErrorActionPreference = 'Stop'

$envFile = Join-Path $PSScriptRoot '.env'
$rsa = [System.Security.Cryptography.RSA]::Create(2048)
$privateKey = [Convert]::ToBase64String($rsa.ExportPkcs8PrivateKey())
$publicKey = [Convert]::ToBase64String($rsa.ExportSubjectPublicKeyInfo())

$lines = if (Test-Path $envFile) { Get-Content $envFile } else { @() }
$lines = $lines | Where-Object { $_ -notmatch '^(JWT_PRIVATE_KEY|JWT_PUBLIC_KEY|JWT_KEY_ID|JWT_ISSUER|JWT_SECRET)=' }
if ($lines.Count -gt 0 -and $lines[-1] -ne '') { $lines += '' }
$lines += '# RS256 keys (infra/generate-jwt-keys.ps1). Private key chỉ dùng cho auth-service.'
$lines += "JWT_PRIVATE_KEY=$privateKey"
$lines += "JWT_PUBLIC_KEY=$publicKey"
$lines += 'JWT_KEY_ID=pc-retail'
$lines += 'JWT_ISSUER=pc-retail'

Set-Content -Path $envFile -Value $lines -Encoding utf8
Write-Host "Updated $envFile (RS256 keypair generated)"
