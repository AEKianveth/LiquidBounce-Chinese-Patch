package t;
import com.google.gson.*;
import com.lbcn.Translator;
import com.lbcn.JsonTree;
import java.util.*;

public class SelfTest {
    static int fail=0;
    static void check(boolean cond,String msg){ if(!cond){System.out.println("FAIL: "+msg);fail++;} else System.out.println("ok: "+msg); }
    public static void main(String[] a){
        Translator tr=Translator.INSTANCE;
        System.out.println("模块映射="+tr.moduleCount()+" 设置映射="+tr.settingCount());
        // 1. 模块往返
        int rt=0; for(String en: Arrays.asList("KillAura","Fly","Velocity","Scaffold","CrystalAura","ClickGUI","HUD")){
            String zh=tr.moduleToZh(en); String back=tr.moduleToEn(zh);
            check(en.equals(back),"模块往返 "+en+"->"+zh+"->"+back); rt++;
        }
        // 2. CHOICE 一致性：枚举所有设置表中的词，模拟 active=choices.key
        JsonObject choices=new JsonObject();
        String[] modes={"Normal","Smart","Legit","Packet","Instant","Custom","Off","UnknownWordXYZ"};
        String active0="Normal";
        for(String m:modes){ JsonObject ch=new JsonObject(); ch.addProperty("name",m); ch.add("value",new JsonArray()); choices.add(m,ch); }
        JsonObject root=new JsonObject();
        root.addProperty("name","Mode"); root.addProperty("active",active0); root.add("choices",choices); root.add("value",new JsonArray());
        JsonTree.translateGroup(root);
        String active=root.get("active").getAsString();
        check(root.getAsJsonObject("choices").has(active),"翻译后 choices[active="+active+"] 可索引");
        // 未收录词保持原样
        check(root.getAsJsonObject("choices").has("UnknownWordXYZ"),"未收录词保留英文原样");
        // 3. 翻译->还原往返后 active 回到 Normal 且可索引
        JsonTree.restoreGroup(root);
        String a2=root.get("active").getAsString();
        check("Normal".equals(a2),"还原后 active=Normal, 实际="+a2);
        check(root.getAsJsonObject("choices").has(a2),"还原后 choices[active] 可索引");
        // 4. 全局：对每个 setting 映射，模拟 active=key 翻译后必然可索引
        int n=0;
        // 通过反射拿 settingZh 的 key 集
        try{
            java.lang.reflect.Field f=Translator.class.getDeclaredField("settingZh"); f.setAccessible(true);
            Map<String,String> mp=(Map<String,String>)f.get(tr);
            int mismatch=0;
            for(String en: mp.keySet()){
                JsonObject c=new JsonObject(); JsonObject child=new JsonObject(); child.addProperty("name",en); child.add("value",new JsonArray()); c.add(en,child);
                JsonObject r=new JsonObject(); r.addProperty("active",en); r.add("choices",c);
                JsonTree.translateGroup(r);
                String av=r.get("active").getAsString();
                if(!r.getAsJsonObject("choices").has(av)) mismatch++;
                n++;
            }
            check(mismatch==0,"全部 "+n+" 个设置词 choices[active] 一致 (mismatch="+mismatch+")");
        }catch(Exception e){e.printStackTrace();fail++;}
        System.out.println(fail==0? "\n=== ALL PASS ===" : "\n=== "+fail+" FAIL ===");
        System.exit(fail==0?0:1);
    }
}
