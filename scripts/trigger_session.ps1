<#
.SYNOPSIS
    Tự động trigger lại conversation session này khi quota hồi phục.
.DESCRIPTION
    Script chờ theo số phút hoặc mốc thời gian chỉ định, sau đó gọi Antigravity CLI (agy)
    để tiếp tục đúng session ID với Gemini model (mặc định: gemini-3.8-flash-medium).
.EXAMPLE
    .\trigger_session.ps1 -WaitMinutes 60
.EXAMPLE
    .\trigger_session.ps1 -TargetTime "20:30"
#>

param (
    [Parameter(Mandatory = $false)]
    [int]$WaitMinutes = 0,

    [Parameter(Mandatory = $false)]
    [string]$TargetTime = "",

    [string]$ConversationId = "61fe36a9-b854-402f-9433-c6a5d544b8ec",

    [string]$Model = "gemini-3.8-flash-medium",

    [string]$Prompt = "Quota đã hồi phục. Hãy: 1) Đọc lại toàn bộ file docs tại D:\Code\PC-Retail\docs\pc-retail-microservices-migration-plan.md, 2) Đối chiếu với source code hiện tại để xác định những gì đã làm và chưa làm, 3) Tiếp tục triển khai các mục còn lại theo thứ tự ưu tiên trong docs. Không cần hỏi lại, cứ làm hết."
)

$ErrorActionPreference = "Stop"

Write-Host "=== ANTIGRAVITY SESSION AUTO-TRIGGER ===" -ForegroundColor Cyan
Write-Host "Target Session : $ConversationId"
Write-Host "Target Model   : $Model"
Write-Host "Current Time   : $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"

if ($TargetTime -ne "") {
    $target = [DateTime]::Parse($TargetTime)
    if ($target -lt (Get-Date)) {
        # Nếu giờ đã qua trong ngày, giả định là ngày mai hoặc cùng ngày
        Write-Warning "Thời gian $TargetTime đã qua hôm nay. Vui lòng kiểm tra lại."
    }
    $diff = $target - (Get-Date)
    if ($diff.TotalSeconds -gt 0) {
        Write-Host "Đang chờ đến mốc $TargetTime (còn $([Math]::Round($diff.TotalMinutes, 1)) phút)..." -ForegroundColor Yellow
        Start-Sleep -Seconds ([int]$diff.TotalSeconds)
    }
} elseif ($WaitMinutes -gt 0) {
    Write-Host "Đang chờ $WaitMinutes phút để quota hồi phục..." -ForegroundColor Yellow
    Start-Sleep -Seconds ($WaitMinutes * 60)
} else {
    Write-Host "Không có thời gian chờ được chỉ định. Đang kích hoạt ngay lập tức..." -ForegroundColor Green
}

Write-Host "`n[$(Get-Date -Format 'HH:mm:ss')] Kích hoạt lại session $ConversationId qua agy CLI..." -ForegroundColor Green

# Gọi agy CLI để resume session
$cmdArgs = @(
    "--conversation", $ConversationId,
    "--model", $Model,
    "--prompt", $Prompt
)

& agy.exe @cmdArgs

Write-Host "[$(Get-Date -Format 'HH:mm:ss')] Đã trigger session thành công!" -ForegroundColor Cyan
