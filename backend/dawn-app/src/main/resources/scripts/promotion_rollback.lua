-- Chỉ INCR nếu key đang tồn tại
for i = 1, #KEYS do
    if redis.call('EXISTS', KEYS[i]) == 1 then
        redis.call('INCRBY', KEYS[i], tonumber(ARGV[i]))
    end
end
return 1