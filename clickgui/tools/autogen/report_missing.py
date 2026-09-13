# -*- coding: utf-8 -*-
# 漏译扫描：对比“全部候选显示名”与“最终词表 + 词素可组合性”，
# 输出最终会在界面显示成英文的标识符，并按 疑似真UI / 明显内部实现类 分类。
import re,sys,os,json,datetime
sys.path.insert(0,os.path.dirname(os.path.abspath(__file__)))
from compose import split_camel,translate
ROOT=os.path.join(os.path.dirname(os.path.abspath(__file__)),'../../')
cand=json.load(open('/tmp/cand2.json',encoding='utf-8'))
zh=json.load(open(ROOT+'resources/zh_all.json',encoding='utf-8'))
def norm(s):
    return re.sub(r"[\s\-]","",re.sub(r"['’`]","",re.sub(r"[\s\-]+(.)",lambda m:m.group(1).upper(),s)))
INTERNAL={'Provider','Handler','Manager','Builder','Subscriber','Consumer','Context','Factory',
'Renderer','Detector','Validator','Impl','Util','Utils','Exception','Request','Response','Adapter',
'Converter','Generator','Sampler','Scanner','Trie','Node','Parameter','Parameters','Params','Inject',
'Runtime','Annotation','Annotations','Listener','Scheduler','Uploader','Uploaded','Spoofer','Encoder',
'Decoder','Loader','Reloader','Tokenizer','Mesh','Atlas','Shader','Vertex','Glyph','Dispatcher',
'Descriptor','Collector','Carrier','Changer','Getter','Hider','Trainer','Triggerer','Estimator',
'Modulator','Modification','Holder','Wrapper','Accessor','Initializer','Continuation','Coroutine',
'Deferred','Resolver','Registry','Repository','Processor','Parser','Lexer','Tokenization','Session',
'Credentials','Authentication','Authorization','Account','Accounts','Presence','Backend','Marketplace',
'Mojang','Microsoft','Altening','Bungeecord','Geyser','Watchdog','Verificator','Promise','Callable',
'Constraint','Constraints','Allocation','Instance','Metadata','Config','Configuration','Property',
'Bootstrap','Snippet','Primitive','Primitives','Stateless','Thread','Async','Mutex','Semaphore',
'Tex','Blit','Lut','Blur','Quad','Sigmoid','Sobel','Depth','Cull','Draw','Uniform','Buffer','Pos',
'Raster','Pipeline','Framebuffer','Stencil','Tessellate','Gpu','Gl','GlState','Src','Bgra','TexCoord',
'Deserializer','Serializer','Impls','Mutable','Subclasses','Cache','Helper','Entry','Flow','Result',
'Jwt','JS','Auth','Agent','Alt','Bearer','Socket','Debugging','Keywords','Errata','Dataset','Batch',
'Asset','Env','Init','Ctx','Kotlin','Unified','Ideographs','CJK','CC','PyTorch','Dinnerbone','CCBlueX',
'Angerable','You','Dollar','Le','Built','Math','Access','H','V','Bn','Ffj','Fff','Kkc','Lkc','Qvb','Sb',
'Yk','K','Mth','Operation','Plugin','Disguised','Construct','ConstructFailResult','Uuid','Int','Py'}
TECH_ABBR=re.compile(r'(JWT|UUID|URL|URI|IP$|RGB|RGBA|HSB|BGRA|SDF|JSON|HTTP|HTTPS|AUTH|PKCE|CEF|JCEF|DOM|CSS|API|SDK|JVM|KOTLIN)')
COMMON_SHORT={'Mode','Type','Name','Item','Block','Color','Range','Delay','Speed','Axis','Both','None','Grid','Hand','Bow','Rod','Pot','Fox','Cat','Wolf','Arm','Leg','Hat','Hud','Gui','ESP','Aim','Air','Bed','Box','Key','Map','Mob','Off','On','Out','Tag','Tap','TPS','MS','HP','FOV','HUD','GUI'}
def is_obf(toks):
    if len(toks)==1 and len(toks[0])<=4:
        w=toks[0]
        if w in COMMON_SHORT: return False
        return bool(re.search(r'[A-Z].*[A-Z]',w)) or (len(w)<=3 and w.lower() not in {'arm','leg','hat','cat','dog','fox','owl','bee','axe','bow','rod','pot','key','map','mob','tag','tap','air','bed','box','bar','end','egg','ice','lag','web','win','fly','hop','run','eat','use','hit','aim'})
    return False
missing=[]
for c in cand:
    if c in zh or norm(c) in zh: continue
    z,miss=translate(c)
    if not miss: continue
    toks=split_camel(c)
    internal=any(t in INTERNAL for t in toks) or bool(TECH_ABBR.search(c)) or c.isupper() or is_obf(toks)
    missing.append((c,toks,miss,internal))
ui=sorted(m for m in missing if not m[3]); internal=sorted(m for m in missing if m[3])
covered=len(cand)-len(missing)
print(f"候选 {len(cand)} | 已翻译 {covered} | 漏译 {len(missing)} = 疑似真UI {len(ui)} + 内部类 {len(internal)}")
for c,toks,miss,_ in ui: print(f"  [需补] {c:32s} 缺:{','.join(miss)}")
json.dump({'ui':[(m[0],m[2]) for m in ui],'internal':[m[0] for m in internal]},open('/tmp/missing_report.json','w'),ensure_ascii=False,indent=1)
