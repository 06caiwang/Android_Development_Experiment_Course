# -*- coding: utf-8 -*-
"""按位置从后往前执行：删图注 / 删游戏主界面段落 / 精简说明文字。"""
import json
import subprocess
import sys

FID = "902f68fe-ae1b-4bb7-a9ac-0d48773b485e"
SDK = "D:/workbuddy/resources/app.asar.unpacked/resources/plugins/workbuddy-builtin/skills/workbuddy-local-office-edit/edsdk.py"

# (begin, end, 'del'|'repl', text)
OPS = [
    # ---- 五、实验结果 ----
    (3356, 3495, "repl", "结果与预期一致：未全部勾选无法进入游戏，勾选后按钮与提示同步变化。在 1080×2220 与 1080×1800 两种屏高下测试，卡片自动伸缩，控件均完整显示。"),
    (3348, 3355, "repl", "（4）结果分析"),
    (3325, 3347, "del", ""),   # 图注（游戏页）
    (3323, 3324, "del", ""),   # 游戏主界面截图
    (3216, 3322, "del", ""),   # 进入游戏主界面的说明
    (3205, 3215, "del", ""),   # 小标题（4）进入游戏主界面
    (3175, 3204, "del", ""),   # 图注（勾选后）
    (3060, 3172, "repl", "两项都勾选后按钮变橙可点，提示文字变为“条款已确认，可以进入战场”；只勾一项时按钮仍为灰色，判断正确。"),
    (3020, 3047, "del", ""),   # 图注（条款页）
    (2907, 3017, "repl", "条款页正文可滚动；未勾选时“同意并开始”按钮为灰色，点击无响应。"),
    (2892, 2906, "repl", "（2）条款确认界面"),
    (2876, 2891, "del", ""),   # 图注（开始界面）
    (2770, 2873, "repl", "运行后进入开始界面：深色渐变背景，中间是图标与“坦克大战”标题，底部橙色“开始游戏”按钮，上方提示需先同意协议。"),

    # ---- 四、实验方案 ----
    (2553, 2701, "del", ""),   # ④ 单独成段
    (2471, 2552, "del", ""),   # ③ 单独成段
    (2388, 2470, "repl", "③ 未勾选时按钮置灰、不可点击，配合橙色提示说明原因；④ 三页在清单文件中锁定竖屏，卡片与按钮圆角统一为 16~22dp。"),
    (2288, 2387, "repl", "① 条款写在 strings.xml 中，段落用 \\n 显式换行，否则换行会被压缩成一个空格；② 卡片底部加渐隐遮罩（fade_bottom.xml），提示正文还有内容。"),
    (2240, 2277, "del", ""),   # 图注（TermsActivity）
    (2009, 2237, "repl", "两个 MaterialCheckBox 共用一个 OnCheckedChangeListener：两项都选中才 setEnabled(true)，按钮由灰变橙，提示文字同步换成“条款已确认，可以进入战场”。"),
    (1957, 1994, "del", ""),   # 图注（activity_terms.xml）
    (1869, 1954, "del", ""),   # CardView 补充段
    (1723, 1868, "repl", "标题栏贴顶、按钮区贴底，中间协议卡片高度设为 0dp 并上下约束，自动占满剩余空间；正文放在 MaterialCardView 的 ScrollView 中，内容过长可滑动。"),
    (1648, 1685, "del", ""),   # 图注（activity_splash.xml）
    (1462, 1645, "repl", "根布局用 FrameLayout 铺满背景，内部竖向 LinearLayout 在 Logo 上方和按钮上方各放一个 weight=1 的 Space，把内容挤到视觉中心、按钮压到底部，免去代码计算屏幕高度。"),
    (1213, 1421, "repl", "三张页面共用主题 Theme.Game（Material3 暗色），主色橙 #FFB020、底色 #080D16，状态栏与导航栏同色；背景统一用 layer-list 叠加“渐变 + 光晕”，三页复用同一个资源。"),
    (1116, 1201, "del", ""),   # 拒绝路径单独成段，并入下一段
    (866, 1115, "repl", "三个页面分别为 MainActivity、TermsActivity 和 GameActivity。条款页勾选后点击“同意并开始”会跳转并结束自身，返回键直接回到开始界面；点击“拒绝并退出”只提示“您未同意协议，无法进入游戏”后退回，不记录任何同意状态。"),
    (610, 852, "repl", "本题目以虚构的“坦克大战”为背景，用三个 Activity 完成“开始界面 → 条款确认 → 游戏主界面”的跳转：点击“开始游戏”先进入条款页，勾选确认后才能进入游戏，主要练习 LinearLayout、FrameLayout、ConstraintLayout 三种布局管理器的搭配使用。"),
]

bad = 0
for begin, end, kind, text in OPS:
    if kind == "del":
        args = {"file_id": FID, "idx": begin}
        cmd = ["python", SDK, "call", "doc_delete_paragraph", "--json", json.dumps(args, ensure_ascii=False)]
    else:
        args = {"file_id": FID, "ranges": [{"begin": begin, "end": end}], "text": text}
        cmd = ["python", SDK, "call", "doc_replace_text", "--json", json.dumps(args, ensure_ascii=False)]
    r = subprocess.run(cmd, capture_output=True, text=True, encoding="utf-8")
    out = (r.stdout or "") + (r.stderr or "")
    ok = "ok" in out and "error" not in out.lower()
    print(("OK  " if ok else "FAIL"), kind, begin, out.strip()[:110])
    if not ok:
        bad += 1

print("failures:", bad)
sys.exit(0)
