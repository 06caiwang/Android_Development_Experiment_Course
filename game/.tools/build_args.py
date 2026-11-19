# -*- coding: utf-8 -*-
"""构建 doc_batch_execute 的 blocks JSON。"""
import json, sys, os

IMG = "D:/AndoridShiyan/game/.preview/"
FID = "C:/Users/zhangyuheng/Desktop/实验报告2.docx"

body_fmt = {"font_family": "宋体", "font_size": 12}
sub_fmt = {"font_family": "黑体", "font_size": 12}
note_fmt = {"font_family": "宋体", "font_size": 9, "color": "595959"}

para_prop = {"jc": "both", "line_spacing": 1.3, "line_spacing_rule": 1,
             "spacing_before": 0, "spacing_after": 0}


def body(text):
    return {"block": "paragraph", "calls": [
        {"tool": "doc_insert_paragraph_with_text",
         "args": {"text": text, "text_format": body_fmt, "paragraph_property": para_prop}},
        {"tool": "doc_modify_paragraph", "args": {"first_line_indent_chars": 2}},
    ]}


def sub(text):
    return {"block": "paragraph", "calls": [
        {"tool": "doc_insert_paragraph_with_text",
         "args": {"text": text, "text_format": sub_fmt, "paragraph_property": para_prop}},
        {"tool": "doc_modify_paragraph", "args": {"first_line_indent_chars": 2}},
    ]}


def image(fname, w, h):
    return {"block": "paragraph", "calls": [
        {"tool": "doc_insert_paragraph_with_text",
         "args": {"runs": [{"type": "image", "image_path": IMG + fname, "w": w, "h": h}],
                  "paragraph_property": {"jc": "center", "spacing_before": 10, "spacing_after": 2}}},
    ]}


def note(text):
    return {"block": "paragraph", "calls": [
        {"tool": "doc_insert_paragraph_with_text",
         "args": {"text": text, "text_format": note_fmt,
                  "paragraph_property": {"jc": "center", "spacing_before": 2, "spacing_after": 10,
                                         "line_spacing": 1.0, "line_spacing_rule": 1}}},
    ]}


PLAN = [
    body("本题目以一个虚构的“坦克大战”游戏为背景，用三个 Activity 串起“开始界面 → 条款确认界面 → 游戏主界面”的跳转流程。用户点击“开始游戏”并不会直接进入游戏，而是先跳转到条款确认页；只有在条款页阅读协议、勾选确认之后，才能进入游戏主界面。题目重点练习 LinearLayout、FrameLayout、ConstraintLayout 三种布局管理器的组合使用，以及 TextView、Button、CheckBox、ScrollView 等常用控件的属性设置与事件绑定。"),

    sub("（1）页面结构与跳转关系"),
    body("三个页面分别对应 MainActivity（activity_splash.xml）、TermsActivity（activity_terms.xml）和 GameActivity（activity_game.xml）。“开始游戏”按钮通过 startActivity(new Intent(this, TermsActivity.class)) 跳转到条款页；“同意并开始”按钮在跳转到游戏界面后调用 finish() 结束条款页，因此从游戏界面按返回键会直接回到开始界面，而不会再次停在条款页。"),
    body("点击“拒绝并退出”或左上角返回键时，只弹出“您未同意协议，无法进入游戏”的提示并退回开始界面，不记录任何同意状态，保证“未同意就不能进入游戏”这一约束在任何路径下都成立。"),

    sub("（2）统一的视觉风格"),
    body("三张页面共用同一个主题 Theme.Game（继承 Theme.Material3.Dark.NoActionBar）：colorPrimary 统一为橙色 #FFB020，页面底色统一为深空蓝黑 #080D16，状态栏与导航栏颜色也改成与背景一致，避免顶部出现一条突兀的白色状态栏。页面背景写在 drawable 的 layer-list 中，用“线性渐变 + 径向光晕”两层叠加，三张页面直接复用同一个背景资源。"),

    sub("（3）开始界面：FrameLayout + LinearLayout 权重分配"),
    body("根布局用 FrameLayout 铺满全屏渐变背景；内部是一个竖向的 LinearLayout，在 Logo 上方和按钮上方各放一个 layout_height=\"0dp\"、layout_weight=\"1\" 的 Space。两块留白把中间内容挤到视觉中心，同时把按钮压在屏幕底部——这样在任意分辨率的手机上，标题始终居中、按钮始终贴底，不需要在代码里计算屏幕高度。"),
    image("code1.png", 330, 524),
    note("activity_splash.xml 中负责“居中 + 贴底”的权重分配"),

    sub("（4）条款界面：ConstraintLayout 约束出“可伸缩”的卡片"),
    body("条款页元素较多（标题栏、协议正文、勾选区、按钮区），用 ConstraintLayout 最合适：标题栏贴顶，按钮区贴底，中间的协议卡片高度设为 0dp，上边约束到标题栏底部、下边约束到勾选区顶部，卡片就会自动占满剩余空间。屏幕高时正文区域更大，屏幕矮时卡片自动收缩，按钮也不会被挤出屏幕。"),
    body("协议正文放在 MaterialCardView 内部并用 ScrollView 包裹，内容超长时可以上下滑动；卡片标题、版本号、滚动提示都放在同一张卡片里，视觉上更整体。"),
    image("code2.png", 420, 521),
    note("activity_terms.xml 中的约束骨架（省略了尺寸与样式属性）"),

    sub("（5）勾选与按钮的联动逻辑"),
    body("两个 MaterialCheckBox 分别对应“我已阅读并同意《坦克大战用户服务协议》”和“我已了解并遵守上述条款，且已年满 12 周岁”，二者共用同一个 OnCheckedChangeListener。回调中判断两个复选框是否同时被选中，只有都为 true 时才调用 setEnabled(true)，按钮由灰色不可点变成橙色可点；同时把提示文字从“请勾选以上两项条款后继续”换成“条款已确认，可以进入战场”，让用户清楚知道还差什么、什么时候可以继续。"),
    image("code3.png", 445, 521),
    note("TermsActivity.java 中两个复选框共用监听器并刷新按钮状态"),

    sub("（6）几处细节处理"),
    body("① 协议正文写在 strings.xml 中，段落之间用 \\n 显式换行。Android 会把源码里的换行和连续空格压缩成一个空格，如果直接在 XML 里敲回车，十几条条款会被挤成一整段，层次全失。"),
    body("② 协议卡片底部叠了一层由卡片底色到透明的渐变遮罩（fade_bottom.xml）。正文还没滑到底时，最后一行文字会自然淡出，暗示下面还有内容，比生硬截断更友好。"),
    body("③ 未勾选时按钮被 setEnabled(false)，MaterialButton 会自动变成灰色，再配合下方的橙色提示文字，用户一眼就能看出按钮为什么点不动。"),
    body("④ 三个 Activity 在 AndroidManifest.xml 中都设置了 android:screenOrientation=\"portrait\"，保证横屏时布局不被拉伸变形；标题栏、卡片、按钮的圆角半径统一在 16~22dp 之间，配色只用深、浅两档灰蓝加一种橙色，整体保持“简约”。"),
]

RESULT = [
    sub("（1）游戏开始界面"),
    body("运行程序后首先进入开始界面：深色渐变背景上居中显示游戏图标、“坦克大战”标题和副标题，底部是橙色的“开始游戏”按钮，按钮上方还有一行灰色提示“开始前需阅读并同意《用户服务协议》”，提前告知用户接下来的流程。"),
    image("fig1_splash.png", 225, 462),
    note("红框①：开始游戏按钮与协议提示"),
    sub("（2）条款确认界面（未勾选）"),
    body("点击“开始游戏”后跳转到条款页。页面中部是可滚动的协议正文，正文最后一条被底部的渐隐遮罩盖住一部分，提示下面还有内容；下方是两个复选框和“请勾选以上两项条款后继续”的提示，“同意并开始”按钮呈灰色，此时点击没有任何响应。"),
    image("fig2_terms.png", 225, 462),
    note("红框①：两个条款复选框　　红框②：置灰、不可点击的按钮"),
    sub("（3）勾选两项后的状态"),
    body("依次勾选两个复选框后，按钮立即变成橙色可点击状态，提示文字也变成青色的“条款已确认，可以进入战场”，说明监听器与状态刷新逻辑工作正常。中间还测试了只勾选其中一项的情况，按钮依然保持灰色，说明两个条件是“与”的关系，判断正确。"),
    image("fig3_terms_ok.png", 225, 462),
    note("红框①：已勾选的两个复选框　　红框②：变为橙色的可点击按钮"),
    sub("（4）进入游戏主界面"),
    body("点击“同意并开始”按钮后成功跳转到游戏主界面：顶部 HUD 显示关卡 01-1、三条生命和得分 0000，中间是当前关卡的简报与难度选择，底部是“开始战斗”按钮。整个跳转过程没有卡顿，也没有出现空白页或元素重叠。"),
    image("fig4_game.png", 225, 462),
    note("红框①：顶部 HUD　　红框②：开始战斗按钮"),
    sub("（5）结果分析"),
    body("以上结果与预期一致：条款未全部勾选时无法进入游戏，勾选后按钮状态与提示文字同步变化，点击后正确跳转，按返回键可正常回到开始界面。另外在 1080×2220 与 1080×1800 两种屏幕高度下分别测试，协议卡片会自动伸缩，勾选区与按钮区始终完整显示，没有出现被挤出屏幕的情况。"),
]

which = sys.argv[1]
idx = int(sys.argv[2])
blocks = PLAN if which == "plan" else RESULT
out = {"file_id": FID, "idx": idx, "blocks": blocks}
path = "args_%s.json" % which
with open(path, "w", encoding="utf-8") as f:
    json.dump(out, f, ensure_ascii=False, indent=1)
print("blocks:", len(blocks), "->", path)
