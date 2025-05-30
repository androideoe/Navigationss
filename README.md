## 目录结构
```text
Navigation/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/ddup/navigation/
│   │   │   │   ├── data/
│   │   │   │   │   ├── repository/  # 数据仓库（部分逻辑实现）
│   │   │   │   │   ├── model/       # data bean
│   │   │   │   ├── di/              # 依赖注入
│   │   │   │   ├── ui/              # UI层
│   │   │   │   │   ├── home/        # 首页地图
│   │   │   │   │   ├── navigation/  # 导航
│   │   │   │   │   ├── summary/     # 行程总结
│   │   │   │   │   └── theme/       # 资源theme
│   │   │   │   └── utils/           # 工具类
│   │   │   └── res/
│   │   └── test/
│   └── build.gradle.kts
├── build.gradle.kts
└── settings.gradle.kts

整体架构采用kotlin + MVVM + JetPack + Flow + Compose实现
整体实现步骤按照以下：
1、确定架构方案
2、接入高德地图 申请key 首先需要在Application设置隐私合规 否则底图白屏
3、获取当前位置 作为发起导航的起点位置 第一次会定位失败 权限未给予 在权限授予后重试
4、首页选点功能 复写底图点击方法 获取经纬度
5、点击开始导航 进入导航页面
6、导航页面路线 显示存在问题 位置可以实时变动（Compose UI实现有点问题）
7、导航有增加gps 信号监测 根据信号强弱显示不同颜色
8、导航总结页基本完成
9、整体页面切换按照currentScreen状态变化实现
10、时间有限 可能存在部分bug
