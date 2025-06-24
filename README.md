# Android多画面数据传递应用

## 项目概述

本项目是一个Android应用，实现了多个画面之间的数据传递功能。应用包含一个主画面和两个子画面，演示了Android中Activity之间的数据传递机制。

## 功能特性

### 1. 主画面 (MainActivity)
- 显示当前数据状态
- 提供两个按钮分别启动不同的子画面
- 接收并显示子画面返回的修改后数据

### 2. 编辑画面 (EditActivity)
- 接收主画面传递的数据
- 提供文本编辑功能
- 支持保存修改后的数据并返回主画面
- 支持取消操作

### 3. 列表画面 (ListActivity)
- 接收主画面传递的数据
- 显示数据的详细信息列表
- 支持点击列表项选择数据返回主画面
- 支持直接返回主画面

## 技术实现

### 数据传递机制

#### 1. 主画面到子画面
使用Intent的`putExtra()`方法传递数据：
```kotlin
val intent = Intent(this, EditActivity::class.java)
intent.putExtra("data", currentData)
editActivityLauncher.launch(intent)
```

#### 2. 子画面到主画面
使用Activity Result API返回数据：
```kotlin
val resultIntent = Intent()
resultIntent.putExtra("edited_data", editedData)
setResult(Activity.RESULT_OK, resultIntent)
finish()
```

#### 3. 主画面接收返回数据
使用ActivityResultContracts处理返回结果：
```kotlin
private val editActivityLauncher = registerForActivityResult(
    ActivityResultContracts.StartActivityForResult()
) { result ->
    if (result.resultCode == Activity.RESULT_OK) {
        val editedData = result.data?.getStringExtra("edited_data")
        if (editedData != null) {
            currentData = editedData
            updateDataDisplay()
        }
    }
}
```

## 项目结构

```
app/src/main/
├── java/com/example/myapplication/
│   ├── MainActivity.kt          # 主画面Activity
│   ├── EditActivity.kt          # 编辑画面Activity
│   └── ListActivity.kt          # 列表画面Activity
├── res/layout/
│   ├── activity_main.xml        # 主画面布局
│   ├── activity_edit.xml        # 编辑画面布局
│   └── activity_list.xml        # 列表画面布局
└── AndroidManifest.xml          # 应用清单文件
```

## 使用说明

1. **启动应用**：应用启动后显示主画面，显示当前数据状态

2. **编辑数据**：
   - 点击"打开编辑画面"按钮
   - 在编辑画面中修改数据
   - 点击"保存并返回"保存修改，或点击"取消"放弃修改
   - 返回主画面后可看到更新的数据

3. **查看列表**：
   - 点击"打开列表画面"按钮
   - 在列表画面中查看数据详情
   - 点击任意列表项选择该项数据返回主画面
   - 或点击"返回主画面"直接返回

## 核心技术点

### 1. Activity生命周期管理
- 正确处理Activity的创建、暂停、恢复和销毁
- 使用`enableEdgeToEdge()`实现全屏显示

### 2. 现代化的Activity Result API
- 替代已废弃的`startActivityForResult()`方法
- 使用`ActivityResultContracts.StartActivityForResult()`
- 类型安全的结果处理

### 3. 用户界面设计
- 使用ConstraintLayout实现响应式布局
- 合理的控件间距和尺寸设计
- 清晰的用户交互流程

### 4. 数据状态管理
- 在主画面中维护数据状态
- 实时更新UI显示
- 正确处理数据的传递和接收

## 扩展功能建议

1. **数据持久化**：使用SharedPreferences或Room数据库保存数据
2. **数据验证**：添加输入数据的格式验证
3. **更多画面类型**：添加更多功能的子画面
4. **动画效果**：添加画面切换动画
5. **主题支持**：支持深色模式和自定义主题

## 编译和运行

1. 使用Android Studio打开项目
2. 确保Android SDK版本为API 30或以上
3. 连接Android设备或启动模拟器
4. 点击运行按钮编译并安装应用

## 系统要求

- **最低SDK版本**：API 30 (Android 11)
- **目标SDK版本**：API 34 (Android 14)
- **开发工具**：Android Studio
- **编程语言**：Kotlin