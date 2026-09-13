# -*- coding: utf-8 -*-
# 扫描整个 net/ccbluex/liquidbounce（模块 + utils/features/config 等共享枚举所在包），
# 提取可能作为 UI 显示名的首字母大写驼峰标识符（枚举 tag / 内部类名 / 常量字符串）。
# 可复现：升级 LB 后重跑。输出 /tmp/cand2.json。
import re,sys,zipfile,json
JAR=sys.argv[1] if len(sys.argv)>1 else '../../libs/liquidbounce.jar'
PFX='net/ccbluex/liquidbounce/'
ident=re.compile(r'^[A-Z][A-Za-z]{2,39}$')
utf=re.compile(rb'[\x20-\x7e]{2,}')
def allcaps(s): return s.replace('_','').isupper()
hard={'LineNumberTable','LocalVariableTable','SourceFile','SourceDebugExtension','StackMapTable',
 'InnerClasses','EnclosingMethod','ConstantValue','RuntimeVisibleAnnotations','Deprecated','Signature',
 'Exceptions','Code','Synthetic','NaN','Infinity'}
cand=set()
with zipfile.ZipFile(JAR) as z:
    names=[n for n in z.namelist() if n.startswith(PFX) and n.endswith('.class')]
    for n in names:
        base=n.split('/')[-1][:-6]
        for seg in base.split('$')[1:]:           # 内部类名
            if ident.match(seg) and not allcaps(seg): cand.add(seg)
        data=z.read(n)
        for m in utf.finditer(data):              # 常量池字符串（含枚举 tag）
            s=m.group().decode('ascii','ignore')
            if ident.match(s) and not allcaps(s): cand.add(s)
cand={c for c in cand if c not in hard and not c.endswith(('Ref','Exception'))}
json.dump(sorted(cand),open('/tmp/cand2.json','w'),ensure_ascii=False)
print("扫描class:",len(names)," 候选标识符:",len(cand))
