-- inventory_rollback.lua
-- KEYS: skuId list
-- ARGV: quantity list (same order)
-- Only INCR if key exists — prevents creating wrong keys

for i = 1, #KEYS do
    local key = 'inventory:available:' .. KEYS[i]
    if redis.call('EXISTS', key) == 1 then
        redis.call('INCRBY', key, tonumber(ARGV[i]))
    end
end
return 1