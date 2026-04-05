# 小熊记账

可爱的粉色系个人记账 App

## 功能特点

- 🌸 樱花粉配色主题 + 新拟态硅胶感UI
- 🐻 小熊图标贯穿全应用
- 💕 底部导航 + 启动页动画
- 📊 收支统计与分类管理（条形图、饼图、折线图）
- 📧 用户反馈功能
- 🔄 GitHub/Gitee 自动版本更新（应用内下载安装）
- ⏰ 定时提醒记账

## 技术栈

- Kotlin 2.0 + Compose
- Hilt 依赖注入
- Room 本地数据库
- Material Design 3

## 构建

```bash
./gradlew assembleDebug
```

APK 输出位置：`app/build/outputs/apk/debug/`

## 发布新版本

当代码有更新需要发布新版本时，执行以下命令：

```bash
# 1. 进入项目目录
cd D:/codes/ad/test1

# 2. 创建版本标签（版本号格式：v主版本.次版本.修订号）
git tag v0.0.5

# 3. 推送到远程仓库（自动触发 GitHub Actions 构建）
git push origin v0.0.5
```

自动流程：
1. GitHub Actions 检测到新 tag
2. 自动构建 APK
3. 自动创建 Release 并上传 APK
4. 自动推送到 Gitee
5. 用户可在 App 内检查更新

## 版本历史

- v0.0.5 - 新拟态硅胶感UI + 应用内下载安装 + Gitee同步
- v0.0.4 - 版本号自动读取 + 统计图表修复
- v0.0.3 - 樱花粉主题 + 底部导航 + 用户反馈 + GitHub 自动更新
- v0.0.2 - 修复Bug
- v0.0.1 - 初始版本

## 反馈与支持

- 问题反馈：设置 → 用户反馈
- 联系邮箱：2760705942@qq.com
