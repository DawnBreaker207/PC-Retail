-- KEYS: danh sách key (đã build sẵn prefix voucher:remaining: hoặc flashsale:remaining:)
-- ARGV: danh sách quantity cần trừ, cùng thứ tự với KEYS
local failed = {}
local n = #KEYS

-- Pha 1: check tất cả trước
for i = 1, n do
    local current = tonumber(redis.call('GET', KEYS[i]))
    if current == nil or current < tonumber(ARGV[i]) then
        table.insert(failed, KEYS[i])
    end
end

if #failed > 0 then
    return failed
end

-- Pha 2: tất cả pass → decrement
for i = 1, n do
    redis.call('DECRBY', KEYS[i], tonumber(ARGV[i]))
end

return {}