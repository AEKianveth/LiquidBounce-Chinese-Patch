# -*- coding: utf-8 -*-
import re,sys,json
sys.path.insert(0,'.')
from morphemes import M
from overrides import OV as _OV_EXTRA
# 整词校准（MC官方译名/语序修正），优先级最高
OV = {
 'Slowness':'缓慢','Haste':'急迫','InstantHealth':'瞬间治疗','JumpBoost':'跳跃提升',
 'Nausea':'反胃','Regeneration':'生命恢复','Resistance':'抗性提升','Strength':'力量',
 'Speed':'速度','NightVision':'夜视','Invisibility':'隐身','FireResistance':'抗火',
 'WaterBreathing':'水肺','MiningFatigue':'挖掘疲劳','Absorption':'伤害吸收','Saturation':'饱和',
 'Poison':'中毒','Wither':'凋零','Levitation':'飘浮','SlowFalling':'缓降','ConduitPower':'潮涌能量',
 'DolphinsGrace':'海豚恩惠','Blindness':'失明','Darkness':'黑暗','HealthBoost':'生命提升',
 'PlaceIn':'放置位置','BottomTop':'从下到上','TopBottom':'从上到下','CloserFirst':'就近优先',
 'FurtherFirst':'从远到近','StatusEffectValueGroup':'状态效果','TemporaryValueGroup':'临时设置',
 'VisualsValueGroup':'外观设置','ScaffoldRotationValueGroup':'脚手架旋转','FogValueGroup':'雾设置',
 'Whitelist':'白名单','Blacklist':'黑名单','Ground':'地面','Placer':'放置器','Try':'尝试',
 'GlobalSettings':'全局设置','Requirements':'生效条件','Planner':'规划器',
}
OV.update(_OV_EXTRA)
def split_camel(s):
    s=re.sub(r'([a-z0-9])([A-Z])',r'\1 \2',s); s=re.sub(r'([A-Z]+)([A-Z][a-z])',r'\1 \2',s)
    return s.split()
def lemma(w):
    if w in M: return M[w]
    for suf,rep in (('ies','y'),('ses','s'),('es',''),('s',''),('ing',''),('ed','')):
        if w.endswith(suf) and len(w)>len(suf)+2:
            c=w[:-len(suf)]+rep
            if c in M: return M[c]
    # 双写辅音去重 hopping->hop
    if len(w)>4 and w[-3]==w[-4] and w.endswith('ing'):
        c=w[:-4]
        if c in M: return M[c]
    return None
def translate(name):
    if name in OV: return OV[name],[]
    out=[];miss=[]
    for w in split_camel(name):
        z=lemma(w)
        if z is None: out.append(w);miss.append(w)
        else: out.append(z)
    return ''.join(out),miss
if __name__=='__main__':
    cand=json.load(open('/tmp/cand2.json',encoding='utf-8'))
    # 全量输出所有候选的自动翻译（不减去上一次 build 产物，避免循环依赖导致词条回退丢失）；
    # 与手工表的优先级由 Export 合并时处理（手工覆盖自动）。
    res={};skip=0;allmiss={}
    for c in cand:
        z,miss=translate(c)
        if miss:  # 闸门：有未命中词素就跳过，不瞎翻
            skip+=1
            for m in miss: allmiss[m]=allmiss.get(m,0)+1
            continue
        res[c]=z
    json.dump(res,open('/tmp/auto_zh.json','w'),ensure_ascii=False)
    print("候选",len(cand)," 自动生成(全量)",len(res)," 闸门跳过",skip)
    print("=== 导致跳过的未命中词素(频次) ===")
    for w,n in sorted(allmiss.items(),key=lambda x:-x[1])[:60]: print(f"{w}:{n}",end='  ')
    print()
