#!/usr/bin/env bash
# 水影汉化 - ClickGUI 阶段构建脚本（JDK25 + 纯 javac，无需 Gradle）
set -e
cd "$(dirname "$0")"
# JDK25：优先使用环境变量 JAVA_HOME，未设置则回退到 PATH 中的 java/javac/jar
if [ -n "$JAVA_HOME" ]; then
  JAVAC="$JAVA_HOME/bin/javac"
  JAVA="$JAVA_HOME/bin/java"
  JAR="$JAVA_HOME/bin/jar"
fi
[ -x "${JAVAC:-}" ] || JAVAC="$(command -v javac)"
[ -x "${JAR:-}" ]  || JAR="$(command -v jar)"
[ -x "${JAVA:-}" ] || JAVA="$(command -v java)"
OUT=build/classes
TOOLS=build/tools
JAR_NAME=liquidbounce-clickgui-cn-1.0.0.jar
CP=$(find libs -name '*.jar' | tr '\n' ':')
rm -rf "$OUT" "$TOOLS"
mkdir -p "$OUT" "$TOOLS" build
# ---- 阶段0：由数据源（Translator 手工表 + tools/data 自动词表）生成前端词条 zh_all.json ----
echo "[build] 生成前端翻译表..."
find tools -name '*.java' > build/tools-sources.txt
"$JAVAC" -encoding UTF-8 --release 25 -cp "$CP" -d "$TOOLS" @build/tools-sources.txt
"$JAVA" -XX:-UseContainerSupport -Dfile.encoding=UTF-8 -cp "$TOOLS:$CP" Export > resources/zh_all.json 2>/dev/null
echo "[build] zh_all.json: $(wc -c < resources/zh_all.json) 字节"
# ---- 阶段1：编译主源码（本阶段 Mixin 只用 JDK 类型，无需 Minecraft 占位类）----
find src -name '*.java' > build/sources.txt
"$JAVAC" -encoding UTF-8 --release 25 -cp "$CP" -d "$OUT" @build/sources.txt
cp resources/fabric.mod.json resources/lbcn.mixins.json resources/zh_all.json "$OUT/"
[ -d resources/assets ] && cp -r resources/assets "$OUT/assets"
rm -f "build/$JAR_NAME"
"$JAR" --create --file "build/$JAR_NAME" -C "$OUT" .
echo "[build] 完成 -> build/$JAR_NAME"
"$JAR" tf "build/$JAR_NAME"
