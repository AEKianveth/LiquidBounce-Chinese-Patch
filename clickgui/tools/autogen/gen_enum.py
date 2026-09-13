# -*- coding: utf-8 -*-
# Tagged 枚举显示值中文表（模块设置下拉/模式/目标/排序等）。大写 name 与 Title Case tag 双形式都生成。
import json,re
T={
# 方向/位置
'ABOVE':'上方','BELOW':'下方','FRONT':'前方','BACK':'后方','LEFT':'左侧','RIGHT':'右侧','TOP':'顶部','BOTTOM':'底部',
'DOWN':'向下','UP':'向上','CENTER':'居中','MIDDLE':'中间','HEAD':'头部','BODY':'身体','FEET':'脚部','EYES':'眼部',
'HORIZONTAL':'水平','VERTICAL':'垂直','DIRECTION':'方向','YAW':'偏航角','PITCH':'俯仰角','POINT':'点','RANGE':'范围',
'DISTANCE':'距离','GROUND':'地面','AIR':'空中','UNDERWATER':'水下','FLOOR':'地面','LANDING':'落地','FALLING':'下落中',
'SINKING':'下沉中','SWUNG':'已挥动','FRONT':'前方',
# 目标/对象
'PLAYER':'玩家','PLAYERS':'玩家','ENTITY':'实体','ENTITIES':'实体','HOSTILE':'敌对','PASSIVE':'友好','FRIENDS':'好友',
'TEAM':'队伍','SELF':'自身','ENEMY':'敌人','ALL':'全部','ANY':'任意','NONE':'无','NOTHING':'无','OTHER':'其他','BOTH':'两者',
'TARGET':'目标','PROJECTILE':'弹射物','PROJECTILES':'弹射物','THROWABLES':'可投掷物','BLOCK':'方块','BLOCKS':'方块',
'LIQUID':'液体','LIQUIDS':'液体','CONTAINER':'容器','INVENTORY':'物品栏','INVENTORIES':'物品栏','ORB':'经验球',
# 时机/开关/通用模式
'ALWAYS':'始终','NEVER':'从不','AUTO':'自动','AUTOMATIC':'自动','ON':'开启','OFF':'关闭','TOGGLE':'切换','HOLD':'按住',
'WAIT':'等待','START':'开始','STOP':'停止','TRIGGER':'触发','DISABLE':'禁用','DEFAULT':'默认','NORMAL':'普通','CONSTANT':'恒定',
'STATIC':'静止','DYNAMIC':'动态','RANDOM':'随机','INFINITE':'无限','SMART':'智能','SILENT':'静默','INSANE':'疯狂',
'STRICT':'严格','SAFE':'安全','EFFICIENT':'高效','SIMPLE':'简单','BASIC':'基础','SUPER':'超级','FULL':'完整','WIDE':'宽',
'LOW':'低','HIGHEST':'最高','LOWEST':'最高' if False else '最低','NEAREST':'最近','FURTHEST':'最远','CLOSEST':'最近',
'ASCENDING':'升序','DESCENDING':'降序','ALPHABETICAL':'按字母','PERCENTAGE':'百分比','AMOUNT':'数量','LENGTH':'长度',
'SECONDS':'秒','TICKS':'游戏刻','PROGRESS':'进度','STATE':'状态','PING':'延迟','HEALTH':'生命','ARMOR':'护甲','DAMAGE':'伤害',
'SPEED':'速度','AGE':'存在时长','NAME':'名称','PACKET':'数据包','CLIENT':'客户端','SERVER':'服务器','LOCAL':'本地','REMOTE':'远程',
'LEGIT':' legit'.strip() if False else ' legit',
# 动作/行为
'ATTACK':'攻击','HIT':'命中','CRITS':'暴击','CRITTED':'已暴击','TF2CRIT':'TF2暴击','KNOCKBACK':'击退','JUMP':'跳跃',
'SPRINT':'疾跑','SNEAK':'潜行','SNEAKING':'潜行中','SWIM':'游泳','SLEEPING':'睡眠中','WATCHING':'注视中','PLAYING':'游戏中',
'LISTENING':'聆听中','MOVE':'移动','BACKWARDS':'向后','FORWARDS':'向前','OMNIDIRECTIONAL':'全方向','OMNIROTATIONAL':'全旋转',
'SWAP':'交换','SWITCH':'切换','CLICK':'点击','DRAG':'拖拽','PLACE':'放置','DESTROY':'破坏','USE':'使用','PICK':'选取',
'PICKUP':'拾取','THROW':'投掷','RELEASE':'松开','INTERACT':'交互','TELEPORT':'传送','DISCONNECT':'断开','QUIT':'退出',
'RESET':'重置','CLEANUP':'清理','EXTEND':'延伸','BRING':'拉回','TOWERING':'搭高','SPAMMING':'刷屏','INCOMING':'传入',
'OUTGOING':'传出','INVERT':'反转','REVERSE':'反向','OVERRIDE':'覆盖','IGNORE':'忽略','CONTAINS':'包含','EQUALS':'等于',
'MATCHES':'匹配','DUPLICATE':'重复','CORRECT':'正确','COMPETING':'竞争中','COLLISION':'碰撞','CLAMP':'钳制','STABILIZED':'稳定',
'CONTROL':'控制','ACTION':'动作','INPUT':'输入','CHAT':'聊天','MESSAGE':'消息','SCREEN':'屏幕','FILE':'文件','SCRIPT':'脚本',
'CONFIG':'配置','THEME':'主题','RESOURCE':'资源','SESSION':'会话','MARKETPLACE':'市场','PLUGINS':'插件','ATTRIBUTES':'属性',
'DETAILS':'详情','TITLE':'标题','SUBTITLE':'副标题','HEADER':'页眉','FOOTER':'页脚','PREFIX':'前缀','LOGO':'标志',
'CAPE':'披风','HAT':'帽子','JACKET':'外套','SHAPE':'形状','TYPE':'类型',
# 武器/工具/材质/方块
'SWORD':'剑','AXE':'斧','PICKAXE':'镐','SHOVEL':'铲','HOE':'锄','BOW':'弓','CROSSBOW':'弩','MACE':'锤','SHIELD':'盾',
'ROD':'杆','PEARL':'末影珍珠','GAPPLE':'金苹果','MILK':'牛奶','FOOD':'食物','POTION':'药水','MAGIC':'魔法','FIRE':'火',
'LAVA':'岩浆','WATER':'水','THUNDER':'雷电','LIGHTNING':'闪电','RAINY':'雨天','SNOWY':'雪天','SUNNY':'晴天','RAINBOW':'彩虹',
'SNOWFLAKE':'雪花','DIAMOND':'钻石','GOLD':'金','IRON':'铁','NETHERITE':'下界合金','LEATHER':'皮革','CHAIN':'锁链',
'ELYTRA':'鞘翅','SADDLE':'鞍','SADDLED':'已装鞍','SLIM':'纤细','SKULL':'头颅','SKULLS':'头颅','PUMPKIN':'南瓜','CACTI':'仙人掌',
'COBWEB':'蜘蛛网','LADDERS':'梯子','BARRIERS':'屏障','GLASS':'玻璃','BRICK':'砖块','BARREL':'木桶','BEACON':'信标',
'CHEST':'箱子','CHESTS':'箱子','FURNACE':'熔炉','DROPPER':'投掷器','DISPENSER':'发射器','HOPPER':'漏斗','SMOKER':'烟熏炉',
'MAGMA':'岩浆块','BLOOD':'血液',
# 状态/效果
'BLINDNESS':'失明','BLINDING':'致盲','DARKNESS':'黑暗','NAUSEA':'反胃','HUNGER':'饥饿','ANGERABLE':'愤怒','INVISIBLE':'隐身',
'DEAD':'死亡','DEATH':'死亡','FAKE':'伪装','FLAG':'标记',
# 时间/版本
'DAY':'白天','NIGHT':'夜晚','NOON':'正午','DAWN':'黎明','DUSK':'黄昏','PAST':'过去','FUTURE':'未来','NEW':'新',
'VANILLA':'原版',
# 账户
'CRACKED':'离线账号','PREMIUM':'正版账号','ALT':'小号',
# 粒子形状
'STAR':'星形','DOLLAR':'美元符','CROWN':'皇冠','HEART':'爱心','LINE':'线条','RHOMBUS':'菱形','SPARK':'火花',
# 逻辑/其他通用
'AND':'与','OR':'或','COIN':'硬币','LINEAR':'线性','BLACKLIST':'黑名单','WHITELIST':'白名单','HOSTING':'托管',
'UNDETECTABLE':'不可检测','MAINHAND':'主手','OFFHAND':'副手','SLASH':'劈砍','SHIFT':'潜行',
'SPACE':'空间','BUTTERFLY':'蝴蝶',
# 模块名引用（与模块中文一致）
'KILLAURA':'杀戮光环','SCAFFOLD':'自动搭桥','BLINK':'瞬移',
# 二轮补漏
'BLOCKING':'格挡中','COMBAT':'战斗','CYRILLIC':'西里尔字母','END':'结束','GAME':'游戏','LEGS':'腿部',
'LIGHTS':'光照','MULTIPLY':'相乘','ONEEIGHT':'1.8','ONEFIFTEEN':'1.15','ONENINE':'1.9','OWNER':'所有者',
'SMOKE':'烟雾','SNAP':'吸附','SPEAR':'矛','WEAPON':'武器',
}
# Legit 为作弊圈通用术语，保留原文
T['LEGIT']='Legit'
# 多词 tag
PH={'Random Case':'随机大小写','Random Space':'随机空格','TF2 Crit':'TF2暴击',
    'Chinese (Simplified)':'简体中文','Chinese (Traditional)':'繁体中文','English (Pirate)':'英语（海盗）',
    'English (US)':'英语（美国）','Dutch (Belgium)':'荷兰语（比利时）','Dutch (Netherlands)':'荷兰语（荷兰）',
    'Portuguese (Brazil)':'葡萄牙语（巴西）'}
# 专名保留（不翻译，显式登记以便覆盖率核对）
KEEP=set('''AIMBOOSTER BLOCKSMC BOYKISSER CUBECRAFT DEXLAND FUNNYMC GRIM HYPIXEL NCP ORBIZ OSU SKEET TEAMHOLY THEALTENING MICROSOFT
BONK BUMP MEOW MOAN POP SLAP SOFT SCHOOLBOY SQUASH TUNG UWU NYA APPLEPAY DAP INSPECT AAC CCBLUEX'''.split())
real=json.load(open('/tmp/tag_real.json'))
def titlecase(s):
    return ' '.join(w[:1].upper()+w[1:].lower() for w in s.split(' '))
out={}
for k,v in T.items():
    if not v:continue
    out[k]=v
    tc=titlecase(k)
    if tc!=k:out[tc]=v
for k,v in PH.items():out[k]=v
# 覆盖率核对
missing=[t for t in real if t not in out and t not in KEEP]
print("高置信tag:",len(real)," 已译:",len([t for t in real if t in out])," 保留专名:",len([t for t in real if t in KEEP]))
print("未处理:",missing)
json.dump(dict(sorted(out.items())),open('tools/data/enum_zh.json','w',encoding='utf-8'),ensure_ascii=False,indent=1)
print("enum_zh 词条:",len(out))
