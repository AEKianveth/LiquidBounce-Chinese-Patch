# -*- coding: utf-8 -*-
# 从前端 bundle 提取所有字符串字面量（状态机，处理转义），供漏译排查
import sys, collections, json

src = open(sys.argv[1], encoding='utf-8').read()
strs = []
i, n = 0, len(src)

def read_quote(q, i):
    j = i + 1
    buf = []
    while j < n:
        c = src[j]
        if c == '\\':
            if j + 1 < n:
                buf.append(src[j + 1]); j += 2; continue
        if c == q:
            return ''.join(buf), j + 1
        buf.append(c); j += 1
    return ''.join(buf), j

while i < n:
    c = src[i]
    if c in '"\'`':
        s, i = read_quote(c, i)
        strs.append(s); continue
    i += 1

cnt = collections.Counter(strs)
json.dump(dict(cnt), open('/tmp/bundle_strings.json', 'w', encoding='utf-8'), ensure_ascii=False)
print("字符串字面量去重:", len(cnt), " 总出现:", sum(cnt.values()))
