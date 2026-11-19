---
name: android-tablelayout-calculator
description: 用 TableLayout 搭建 Android 计算器界面（浅色卡片+胶囊按键）并实现加减乘除运算。当需要在 Android 原生工程里做"计算器界面/四则运算布局/按键网格"时使用。
---

# TableLayout 计算器开发技能

## 1. 适用场景

- 课程实验：要求"用 TableLayout 实现计算器界面"
- 需要 5 行 × 4 列等宽按键网格，且能真正完成 `+ - × ÷` 运算
- 目标风格：浅灰底 + 白色胶囊按键 + 橙色强调（iOS/小米计算器风格）

## 2. 目标产物（文件契约）

| 文件 | 职责 |
| --- | --- |
| `res/layout/activity_main.xml` | 显示区 + TableLayout 键盘（5 TableRow × 4 键） |
| `res/values/styles.xml` | `CalcKey` 按键样式，避免 20 个控件重复写属性 |
| `res/drawable/bg_key.xml` | 白色胶囊 + 涟漪（ripple） |
| `res/drawable/bg_key_accent.xml` | 橙色渐变胶囊（等号键） |
| `res/drawable/ic_backspace.xml` | 退格矢量图标 |
| `res/values/colors.xml` | 设计令牌（颜色） |
| `java/.../CalcEngine.java` | 输入状态机 + 实时结果刷新（纯 Java，可单测） |
| `java/.../ExpressionEvaluator.java` | 表达式求值（递归下降 + BigDecimal） |
| `java/.../MainActivity.java` | 找到 20 个按键、统一监听、渲染显示 |

## 3. 视觉规范

| 令牌 | 值 | 用途 |
| --- | --- | --- |
| `bg_page` | `#F4F5F7` | 页面背景 |
| `key_bg` | `#FFFFFF` | 按键底色 |
| `text_primary` | `#1C1C1E` | 数字 |
| `text_secondary` | `#9AA0A6` | 表达式、"＝" 符号 |
| `accent` | `#FF8000` | 运算符 / C / 退格 |
| 渐变 | `#FFA227 → #FF7A00` | 等号键 |

尺寸：按键高 `62dp`、外边距 `5dp`、页面左右内边距 `7~12dp`；圆角统一用 `radius=1000dp`（GradientDrawable 会自动裁剪到短边一半 ⇒ 完美胶囊，不用按尺寸算圆角）。

字号：数字/运算符 `28~30sp`、等号 `32sp`、表达式 `22sp`、结果 `56sp`（autoSize 22~56sp），字体 `sans-serif-light`。

## 4. 布局结构

```
LinearLayout(vertical, bg_page)
├── LinearLayout(weight=1, gravity=bottom|end)      显示区，吃掉剩余高度
│   ├── AppCompatTextView tv_expression   "1+2"
│   └── LinearLayout(horizontal, gravity=end)
│       ├── TextView "="  26sp 灰色
│       └── AppCompatTextView tv_result  "3" autoSize
└── TableLayout(stretchColumns="*", shrinkColumns="*")
    ├── TableRow  C      ⌫      %      ÷
    ├── TableRow  7      8      9      ×
    ├── TableRow  4      5      6      −
    ├── TableRow  1      2      3      +
    └── TableRow  00     0      .      ＝
```

关键点：

1. **列等宽**：`TableRow` 继承 `LinearLayout`，子键 `layout_width=0dp + layout_weight=1`，配合 `stretchColumns="*"` 即可四等分。
2. **行高固定**：不要给 `TableRow` 用 `0dp + weight`（TableLayout 自行测量行高，权重在 UNSPECIFIED 下会算成 0 ⇒ 控件消失）。行高交给按键的 `62dp` 决定，剩余空间由上方显示区的 `weight=1` 吸收。
3. **显式用 `AppCompatButton`**：Material3 主题会把 `<Button>` 自动替换成 `MaterialButton`，它会**忽略** `android:background`（改走 backgroundTint），自定义胶囊就失效了。
4. `minWidth/minHeight/padding` 全部归零，`includeFontPadding=false`，否则键面文字会偏。
5. `stateListAnimator=@null` 去掉默认按压升降动画，改由 ripple 提供反馈。

## 5. 交互规格

| 按键 | tag | 行为 |
| --- | --- | --- |
| `0-9`、`00` | `"7"`、`"00"` | 追加数字；当前数为 `0` 时替换，避免 `007` |
| `.` | `"."` | 每个数最多一个小数点；数为空时自动补 `0.` |
| `+ - × ÷` | `"+ - * /"` | 追加；末尾已是运算符则**替换**；空表达式时只允许 `-`（负数开头） |
| `%` | `"%"` | 当前数 ÷100 后回写表达式 |
| `C` | `"C"` | 清空 |
| `⌫` | `"DEL"` | 删一个字符；刚按过 `=` 则视为清空 |
| `=` | `"="` | 置 `justEvaluated=true`，保留完整表达式 + 结果 |

状态机三原则：
- 每次输入后都重算一次"预览结果"（尾随运算符先剥掉），所以 `1+2` 没按等号也已经显示 `=3`，与参考图一致。
- 按 `=` 后再按数字 ⇒ 开新算式；按运算符 ⇒ 在结果上继续算（`1+2` → `1+2*`，语义自洽）。
- 除以 0 / 表达式非法 ⇒ 结果位显示"错误"并标红，用户按 `C` 或继续输入即可恢复。

## 6. 运算引擎

**不能用 `ScriptEngine`**（Android 无 Nashorn），自己写：

```
evaluate(expr):
  tokenize  → 数字串 / 运算符
  expr    := term (('+'|'-') term)*
  term    := factor (('*'|'/') factor)*
  factor  := ('+'|'-')? number
  number  := BigDecimal(token)
  '/'     → 除数为 0 抛异常；否则 divide(19, HALF_UP)
  输出    → stripTrailingZeros + toPlainString，超 14 位有效数字再按 MathContext(10) 舍入
```

用 `BigDecimal` 而不是 `double`：`0.1+0.2` 必须显示 `0.3`，不能是 `0.30000000000000004`。
`BigDecimal.divide` 必须给 scale/rounding，否则 `1/3` 直接抛 `ArithmeticException`。

## 7. 实施步骤

1. `colors.xml` / `themes.xml` 落地颜色与浅色主题（`Theme.Material3.Light.NoActionBar`，状态栏同色 + `windowLightStatusBar`），并把 `values-night/themes.xml` 去掉，避免深色下白键变黑字看不清。
2. 写 3 个 drawable（`bg_key` / `bg_key_accent` / `ic_backspace`），ripple 的 `<mask>` 必须是纯色 shape，否则涟漪会溢出圆角。
3. `styles.xml` 定义 `CalcKey`（含 layout 参数）、`CalcKey.Accent`、`CalcKeyIcon`。
4. `activity_main.xml`：显示区 + TableLayout，20 个键都用 `android:tag` 标注语义。
5. `ExpressionEvaluator` → `CalcEngine` → `MainActivity`（遍历 `KEY_IDS` 统一注册监听，`switch(tag)` 分发）。
6. 在 `onSaveInstanceState` 里保存表达式字符串，转屏不丢内容。
7. 验证：纯 Java harness 覆盖 `1+2=3`、`0.1+0.2=0.3`、`2+3×4=14`、`10÷4=2.5`、`5÷0=错误`、`00`、`=` 后接数字。

## 8. 验收标准

- [ ] 界面 5×4 全等宽、按键是胶囊形、等号键橙色渐变
- [ ] `1+2` 实时显示 `=3`，按 `=` 后数字/表达式保持在屏
- [ ] 优先级正确：`2+3×4 = 14`
- [ ] 小数精度正确：`0.1+0.2 = 0.3`
- [ ] 除零提示"错误"，`C` 可恢复
- [ ] 退格、`%`、`00`、`=` 后继续运算均无异常
- [ ] 转屏后算式不丢

## 9. 常见坑速查

| 现象 | 原因 / 解法 |
| --- | --- |
| 按键背景不生效、仍是圆角矩形纯色 | `<Button>` 被 Material3 换成 MaterialButton，用 `AppCompatButton` |
| 整行按键高度为 0 不显示 | TableRow 用了 `0dp+weight`，改固定高度 |
| 涟漪是方形溢出胶囊 | ripple 少了 `<item android:id="@android:id/mask">` |
| 结果文字超出屏幕 | 用 `app:autoSizeTextType`（需 AppCompatTextView） |
| 深色模式配色崩了 | DayNight 主题 + 硬编码浅色，改 Light 或删 values-night |
| `1/3` 崩溃 | `BigDecimal.divide` 没传 scale/RoundingMode |
| `0.1+0.2=0.30000000000000004` | 用了 double，改 BigDecimal |
