-- inventory_decrement.lua
-- KEYS: "{warehouseId}:{skuId}" composite keys
-- ARGV: quantity list (same order as KEYS)
-- Returns: list of failed composite keys (empty = all passed)

local failed = {}
local n = #KEYS

-- Phase 1: check all before decrementing any
for i = 1, n do
    local key = 'inventory:available:' .. KEYS[i]
    local current = tonumber(redis.call('GET', key))
    if current == nil or current < tonumber(ARGV[i]) then
        table.insert(failed, KEYS[i])
    end
end

-- If any item failed → decrement nothing
if #failed > 0 then
    return failed
end

-- Phase 2: all passed → decrement
for i = 1, n do
    local key = 'inventory:available:' .. KEYS[i]
    redis.call('DECRBY', key, tonumber(ARGV[i]))
end

return {}