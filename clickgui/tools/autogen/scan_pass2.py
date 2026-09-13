#!/usr/bin/env python3
"""第二轮复扫：Ot(el,显示属性,动态值) 未过__zh + O()模板内 >英文文本节点<"""
import re, json, sys, zipfile, os

JAR = "build/liquidbounce-clickgui-cn-1.0.0.jar"
ZH = json.load(open("resources/zh_all.json"))

# 提取 bundle
with zipfile.ZipFile(JAR) as z:
    names = z.namelist()
    bundle_names = [n for n in names if 'bundle' in n.lower() and n.endswith('.js')]
    print(f"bundle files: {bundle_names}")
    src = ""
    for bn in bundle_names:
        src += z.read(bn).decode('utf-8', errors='replace') + "\n"
print(f"bundle total len: {len(src)}")

# 品牌/专名白名单（有意保留）
BRAND = set('''ClickGUI Click GUI Discord GitHub LiquidBounce liquidbounce.net Microsoft Realms TheAltening Twitter ViaFabricPlus YouTube Cracked Offline Online Premium
SOCKS4 SOCKS5 HTTP HTTPS TCP UDP DNS IPv4 IPv6 UUID URL IP MAC
Fabric Forge NeoForge Quilt
Geyser Lunar CheatBreaker Cheat Breaker Vanilla BungeeCord Bungee Cord
Mojang Mojang Studios Xbox Xbox Live Bedrock Java Edition
NYA OSU UWU Bonk Boykisser Meow Moan Tung Schoolboy Skeet Skeet2 Aimbooster TF2 TF2 Crit Crit
Cyrillic Latin CJK
'''.split())

def is_brand(s):
    s2 = s.strip()
    if not s2: return True
    if s2 in BRAND: return True
    # 纯技术值
    if re.fullmatch(r'[A-Z0-9_\-\.\/:]+', s2): return True
    # 纯数字/版本
    if re.fullmatch(r'[\d\.\-vV]+', s2): return True
    return False

# === 方法A：Ot(el, "显示属性", 动态表达式) 扫描 ===
# Ot 是设置元素属性的函数，形式 Ot(element, "title"/"alt"/"aria-label"/"placeholder"/"label", value)
ot_pattern = re.compile(r'Ot\s*\(\s*([^,]+?)\s*,\s*"(title|alt|aria-label|placeholder|label|text)"\s*,\s*([^)]+)\)')
ot_hits = []
for m in ot_pattern.finditer(src):
    elem, attr, val = m.group(1), m.group(2), m.group(3).strip()
    # 检查值是否过了 __zh 或 qt(
    if '__zh' in val or 'qt(' in val or 'globalThis.__zh' in val:
        continue
    # 跳过纯字符串常量且是品牌
    if val.startswith('"') and val.endswith('"'):
        literal = val[1:-1]
        if is_brand(literal):
            continue
        # 检查是否在词典中
        if literal in ZH:
            continue
    ot_hits.append((attr, val[:80], m.start()))

print(f"\n=== 方法A: Ot动态属性未翻译 ===")
print(f"命中数: {len(ot_hits)}")
for attr, val, pos in ot_hits[:30]:
    print(f"  [{attr}] {val}  @{pos}")

# === 方法B：O()模板内 >英文文本节点< ===
# O('div', {...}, 'Text') 或 O('div', 'Text') 形式的纯文本子节点
# 找 O(tag, props?, "纯英文文本") 第三个参数是字符串字面量
text_node_pattern = re.compile(r"O\s*\(\s*'([a-zA-Z][a-zA-Z0-9-]*)'\s*,\s*(\{[^}]*\}|null|undefined)?\s*,\s*\"([A-Za-z][A-Za-z0-9 ,\.\-\!'?/()&:]+)\"\s*\)")
text_hits = []
for m in text_node_pattern.finditer(src):
    tag, props, text = m.group(1), m.group(2) or '', m.group(3).strip()
    if not text or len(text) < 2: continue
    if is_brand(text): continue
    if text in ZH: continue
    # 检查上下文是否已有 __zh 包裹（不太可能在纯字符串里）
    text_hits.append((tag, text[:80], m.start()))

print(f"\n=== 方法B: O()模板英文文本节点 ===")
print(f"命中数: {len(text_hits)}")
for tag, text, pos in text_hits[:40]:
    print(f"  <{tag}> \"{text}\"  @{pos}")

# === 方法C：ft(elem, 字符串字面量) 硬编码英文 ===
ft_lit_pattern = re.compile(r"ft\s*\(\s*[^,]+?\s*,\s*\"([A-Za-z][A-Za-z0-9 ,.\-!'?/()&:]+)\"\s*\)")
ft_hits = []
for m in ft_lit_pattern.finditer(src):
    text = m.group(1).strip()
    if not text or len(text) < 2: continue
    if is_brand(text): continue
    if text in ZH: continue
    # 检查前面是否有 __zh(
    ctx = src[max(0,m.start()-30):m.start()]
    if '__zh' in ctx or 'qt(' in ctx:
        continue
    ft_hits.append((text[:80], m.start()))

print(f"\n=== 方法C: ft硬编码英文字符串 ===")
print(f"命中数: {len(ft_hits)}")
for text, pos in ft_hits[:40]:
    print(f"  \"{text}\"  @{pos}")

print(f"\n=== 总结 ===")
print(f"Ot未译: {len(ot_hits)}, 文本节点未译: {len(text_hits)}, ft硬编码未译: {len(ft_hits)}")
total = len(ot_hits)+len(text_hits)+len(ft_hits)
print(f"总计疑似漏译: {total}")
