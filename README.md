# 小熊记账

可爱的粉色系个人记账 App

## 功能特点

- 🌸 樱花粉配色主题
- 🐻 小熊图标贯穿全应用
- 💕 底部导航 + 启动页动画
- 📊 收支统计与分类管理
- 📧 用户反馈功能
- 🔄 GitHub 自动版本更新

## 技术栈

- Kotlin 2.0 + Compose
- Hilt 依赖注入
- Room 本地数据库
- Material Design 3

## 构建

```bash
./gradlew assembleDebug
```

APK 输出位置：`app/build/intermediates/apk/debug/app-debug.apk`

## 发布新版本

当代码有更新需要发布新版本时，执行以下命令：

```bash
# 1. 进入项目目录
cd D:/codes/ad/test1

# 2. 创建版本标签（版本号格式：v主版本.次版本.修订号）
git tag v0.0.3

# 3. 推送到远程仓库（自动触发 GitHub Actions 构建）
git push origin v0.0.3
```

自动流程：
1. GitHub Actions 检测到新 tag
2. 自动构建 APK
3. 自动创建 Release 并上传 APK
4. 用户可在 App 内检查更新

## 版本历史

- v0.0.2 - 樱花粉主题 + 底部导航 + 用户反馈 + GitHub 自动更新
- v0.0.1 - 初始版本

## 反馈与支持

- 问题反馈：设置 → 用户反馈
- 联系邮箱：2760705942@qq.com
