import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.lbcn.Translator;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

/**
 * 构建期工具：从单一数据源 Translator 导出前端用英→中词条 JSON。
 * 不打进最终 jar，仅在 build.sh 中运行生成 resources/zh_all.json。
 *
 * 合并优先级（后者覆盖前者）：
 *   1) tools/data/auto_zh.json —— 由 tools/autogen 离线生成的“词素组合”补漏表（覆盖深层
 *      Choice/枚举/ValueGroup 选项名），只填手工表遗漏，质量以手工表为准；
 *   2) settingZh 手工设置项；3) moduleZh 手工模块名；4) 分类与通用词。
 */
public class Export {
    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        Translator t = Translator.INSTANCE;
        Field fm = Translator.class.getDeclaredField("moduleZh"); fm.setAccessible(true);
        Field fs = Translator.class.getDeclaredField("settingZh"); fs.setAccessible(true);
        Map<String, String> all = new TreeMap<>();

        // 1) 自动补漏表（优先级最低）
        Type mt = new TypeToken<TreeMap<String, String>>(){}.getType();
        for (String res : new String[]{"tools/data/auto_zh.json", "tools/data/geo_zh.json", "tools/data/enum_zh.json", "tools/data/enum_extra_zh.json", "tools/data/hud_zh.json", "tools/data/hud_extra_zh.json", "tools/data/hud_desc_zh.json", "tools/data/sound_zh.json", "tools/data/group_zh.json", "tools/data/spaced_zh.json", "tools/data/packet_zh.json", "tools/data/base_zh.json"}) {
            Path p = Path.of(res);
            if (Files.exists(p)) {
                Map<String, String> m = new GsonBuilder().create()
                        .fromJson(Files.readString(p), mt);
                all.putAll(m);
                System.err.println("[Export] 合并 " + res + ": " + m.size() + " 条");
            } else {
                System.err.println("[Export] 警告: 未找到 " + p + "，跳过");
            }
        }

        // 先放设置/模式，再放模块：模块名与设置项重名时（共 17 个）以模块译名为准，
        // 保证 ClickGUI 模块列表主体准确；重名设置项数量极少且多位于同名模块内。
        all.putAll((Map<String, String>) fs.get(t));
        all.putAll((Map<String, String>) fm.get(t));
        // 分类名（ClickGUI 左侧分类）
        all.put("Combat", "战斗"); all.put("Movement", "移动"); all.put("Player", "玩家");
        all.put("Render", "渲染"); all.put("World", "世界"); all.put("Misc", "其他");
        all.put("Fun", "娱乐"); all.put("Exploit", "漏洞"); all.put("Ghost", "幽灵");
        all.put("Client", "客户端");
        // 通用 UI / 选项词（显示层翻译；数据 name 仍保持英文，不影响前端逻辑匹配）
        java.util.Map<String,String> general = java.util.Map.ofEntries(
                Map.entry("Enabled", "启用"), Map.entry("Disabled", "禁用"),
                Map.entry("Prompt", "询问"), Map.entry("None", "无"),
                Map.entry("Both", "两者"), Map.entry("Bind", "绑定"),
                Map.entry("Reset", "重置"), Map.entry("On", "开"), Map.entry("Off", "关"),
                Map.entry("Yes", "是"), Map.entry("No", "否"),
                Map.entry("Always", "总是"), Map.entry("Never", "从不"),
                Map.entry("Automatic", "自动"), Map.entry("Manual", "手动"),
                Map.entry("Horizontal", "水平"), Map.entry("Vertical", "垂直"),
                Map.entry("Up", "上"), Map.entry("Down", "下"),
                Map.entry("Left", "左"), Map.entry("Right", "右"),
                Map.entry("Forward", "向前"), Map.entry("Backward", "向后"),
                Map.entry("North", "北"), Map.entry("South", "南"),
                Map.entry("East", "东"), Map.entry("West", "西"),
                Map.entry("Smooth", "平滑"), Map.entry("Linear", "线性"),
                Map.entry("Instant", "瞬间"), Map.entry("Hold", "按住"),
                Map.entry("Toggle", "切换"), Map.entry("Permanent", "永久"));
        all.putAll(general);
        System.out.print(new GsonBuilder().disableHtmlEscaping().create().toJson(all));
    }
}
