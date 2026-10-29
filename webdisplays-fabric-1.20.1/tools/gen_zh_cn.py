# -*- coding: utf-8 -*-
"""Generates zh_cn.json for WebDisplays (Fabric 1.20.1) from the modern en_us.json keys."""
import json, collections, io, os

LANG_DIR = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources", "assets", "webdisplays", "lang")
en = json.load(io.open(os.path.join(LANG_DIR, "en_us.json"), encoding="utf-8"))

zh = collections.OrderedDict()

def add(key, text):
    zh[key] = text

# Advancements
add("advancements.webdisplays.keyboardcat.title", "该死的猫")
add("advancements.webdisplays.keyboardcat.description", "让一只猫从你的键盘上走过")
add("advancements.webdisplays.laser.title", "别照着别人的眼睛")
add("advancements.webdisplays.laser.description", "制作一支激光笔！")
add("advancements.webdisplays.linkperipheral.title", "它变成无线的了！")
add("advancements.webdisplays.linkperipheral.description", "把一个外部设备连接到屏幕上")
add("advancements.webdisplays.minepad.title", "这是一场革命")
add("advancements.webdisplays.minepad.description", "亲手拿到最新科技产品：掌上电脑")
add("advancements.webdisplays.minepad2.title", "鸽子")
add("advancements.webdisplays.minepad2.description", "制作一台第二代掌上电脑。我知道它贵得离谱，但贵就代表比别人强，对吧？……对吧？")
add("advancements.webdisplays.padbreak.title", "逆向工程")
add("advancements.webdisplays.padbreak.description", "这玩意儿很脆！把它从高台上摔下去，以解锁升级配方")
add("advancements.webdisplays.root.title", "WebDisplays")
add("advancements.webdisplays.root.description", "WebDisplays 模组")
add("advancements.webdisplays.screen.title", "互联网是用来……")
add("advancements.webdisplays.screen.description", "制作你的第一个网页屏幕")
add("advancements.webdisplays.upgrade.title", "不只是一块屏幕")
add("advancements.webdisplays.upgrade.description", "安装你的第一个升级组件")

# Blocks
add("block.webdisplays.ccinterface", "ComputerCraft 接口")
add("block.webdisplays.cointerface", "OpenComputers 接口")
add("block.webdisplays.kb_left", "键盘")
add("block.webdisplays.peripheral", "外部设备")
add("block.webdisplays.rctrl", "远程控制器")
add("block.webdisplays.redctrl", "红石控制器")
add("block.webdisplays.screen", "网页屏幕方块")
add("block.webdisplays.server", "服务器方块")

# Items
add("item.webdisplays.advicon", "进度图标")
add("item.webdisplays.advicon.brokenpad", "损坏的掌上电脑")
add("item.webdisplays.advicon.pigeon", "鸽子")
add("item.webdisplays.advicon.wd", "内置网页浏览器")
add("item.webdisplays.craftcomp", "制作材料")
add("item.webdisplays.craftcomp_backlight", "屏幕背景")
add("item.webdisplays.craftcomp_badextcard", "损坏的扩充卡片")
add("item.webdisplays.craftcomp_batcell", "电池")
add("item.webdisplays.craftcomp_batpack", "电池组")
add("item.webdisplays.craftcomp_extcard", "扩充卡片")
add("item.webdisplays.craftcomp_laserdiode", "650 纳米激光二极管")
add("item.webdisplays.craftcomp_peripheral", "外部设备材料")
add("item.webdisplays.craftcomp_stonekey", "石头按钮")
add("item.webdisplays.craftcomp_upgrade", "空白升级组件")
add("item.webdisplays.laserpointer", "激光笔")
add("item.webdisplays.linker", "连接工具")
add("item.webdisplays.minepad", "掌上电脑")
add("item.webdisplays.minepad2", "第二代掌上电脑")
add("item.webdisplays.ownerthief", "屏幕所有权给予器 [管理员物品]")
add("item.webdisplays.screencfg", "屏幕配置器")
add("item.webdisplays.upgrade", "屏幕升级组件")
add("item.webdisplays.upgrade_gps", "GPS 模块")
add("item.webdisplays.upgrade_lasermouse", "激光笔模块")
add("item.webdisplays.upgrade_redinput", "红石输入模块")
add("item.webdisplays.upgrade_redoutput", "红石输出模块")
add("item.webdisplays.wiki", "按“F1”打开 Wiki")

add("itemGroup.webdisplays", "§5内置网页浏览器")

# Config screen
add("webdisplays.config.screen.title", "WebDisplays 设置")
add("webdisplays.config.screen.category.common", "通用")
add("webdisplays.config.screen.category.client", "客户端")

# Config entry labels (referenced by AnnoCFG @Translation keys)
add("config.webdisplays.hard_recipes", "困难配方")
add("config.webdisplays.disable_thief", "禁用所有权给予器")
add("config.webdisplays.blacklist", "域名黑名单")
add("config.webdisplays.home_page", "主页")
add("config.webdisplays.max_res_x", "最大水平分辨率")
add("config.webdisplays.max_res_y", "最大垂直分辨率")
add("config.webdisplays.max_width", "最大宽度（方块）")
add("config.webdisplays.max_height", "最大高度（方块）")
add("config.webdisplays.miniserv_port", "MiniServ 端口")
add("config.webdisplays.miniserv_quota", "MiniServ 配额")
add("config.webdisplays.load_distance", "加载距离")
add("config.webdisplays.unload_distance", "卸载距离")
add("config.webdisplays.pad_res", "掌上电脑分辨率")
add("config.webdisplays.side_pad", "掌上电脑侧持")
add("config.webdisplays.switch_buttons", "交换左右键")
add("config.webdisplays.auto_vol", "启用自动音量")
add("config.webdisplays.yt_vol", "YouTube 音量")
add("config.webdisplays.d0", "无声距离")
add("config.webdisplays.d100", "开始衰减距离")

# Extension card / minePad tooltips
add("webdisplays.extcard.bad", "有人把扩充卡片做坏了")
add("webdisplays.extcard.cantcraft1", "你的知识还不够。")
add("webdisplays.extcard.cantcraft2", "你一定会把这个物品做失败。")
add("webdisplays.minepad.turnon", "潜行并右键以开机")
add("webdisplays.minepad2.info", "概不退换！")

# Keyboard GUI
add("webdisplays.gui.keyboard.gotcha", "抓到你了")
add("webdisplays.gui.keyboard.hooked", "键盘已连接。按 Esc 退出。")
add("webdisplays.gui.keyboard.warning1", "警告！在此键盘上输入的内容会以明文发送到服务器。")
add("webdisplays.gui.keyboard.warning2", "也就是说，任何人都可能知道你在做什么。")
add("webdisplays.gui.keyboard.warning3", "千万不要用这个键盘输入你的任何密码。")

# Screen configurator
add("webdisplays.gui.screencfg.autovol", "自动音量")
add("webdisplays.gui.screencfg.avwarning", "§c注意！\n自动音量只对 YouTube 视频生效，\n并且需要在客户端配置中开启！")
add("webdisplays.gui.screencfg.click", "点击与输入")
add("webdisplays.gui.screencfg.friendlist", "管理好友")
add("webdisplays.gui.screencfg.friends", "好友：")
add("webdisplays.gui.screencfg.lockratio", "锁定比例")
add("webdisplays.gui.screencfg.mres", "修改分辨率")
add("webdisplays.gui.screencfg.mupgrades", "升级与连接")
add("webdisplays.gui.screencfg.otherrights", "管理他人权限")
add("webdisplays.gui.screencfg.others", "其他玩家")
add("webdisplays.gui.screencfg.owner", "屏幕拥有者：")
add("webdisplays.gui.screencfg.permissions", "权限：")
add("webdisplays.gui.screencfg.resolution", "分辨率：")
add("webdisplays.gui.screencfg.rot0", "0°")
add("webdisplays.gui.screencfg.rot90", "90°")
add("webdisplays.gui.screencfg.rot180", "180°")
add("webdisplays.gui.screencfg.rot270", "270°")
add("webdisplays.gui.screencfg.rotation", "旋转")
add("webdisplays.gui.screencfg.setres", "设置分辨率")
add("webdisplays.gui.screencfg.seturl", "修改网址")
add("webdisplays.gui.screencfg.upgrades", "升级组件：")

# Set URL GUI
add("webdisplays.gui.seturl.cancel", "取消")
add("webdisplays.gui.seturl.ok", "确定")
add("webdisplays.gui.seturl.shutdown", "关机")
add("webdisplays.gui.seturl.url", "网址：")

# Linking tool
add("webdisplays.linker.posInfo", "屏幕坐标：%d %d %d")
add("webdisplays.linker.selectPeripheral", "右键点击一个外部设备")
add("webdisplays.linker.selectScreen", "右键点击一个屏幕")
add("webdisplays.linker.sideInfo", "朝向：%s")

# Messages
add("webdisplays.message.chunkUnloaded", "屏幕所在区块尚未加载！")
add("webdisplays.message.invalid", "结构无效；请看你准星指向的坐标 %s。")
add("webdisplays.message.linkAbort", "连接工具已重置")
add("webdisplays.message.linkError", "连接失败 :（请查看日志……")
add("webdisplays.message.linked", "连接成功！")
add("webdisplays.message.missingCC", "ComputerCraft 不可用。")
add("webdisplays.message.missingOC", "OpenComputers 不可用。")
add("webdisplays.message.newOwner", "你现在是这个屏幕的拥有者！")
add("webdisplays.message.noMiniserv", "本服务器已禁用服务器方块")
add("webdisplays.message.notAScreen", "请先右键点击屏幕……")
add("webdisplays.message.notLinked", "这个外部设备还没有被连接。")
add("webdisplays.message.otDisabled", "本服务器已禁用所有权给予器")
add("webdisplays.message.peripheral", "这不是外部设备！")
add("webdisplays.message.restrictions", "你没有权限这样做 :（")
add("webdisplays.message.screenSet", "屏幕已设置！现在把这个物品交给新的拥有者……")
add("webdisplays.message.screenSet2", "屏幕已设置！现在右键点击外部设备……")
add("webdisplays.message.tooBig", "太大了！最大尺寸是 %dx%d。")
add("webdisplays.message.tooSmall", "太小了！最小尺寸是 2x2。")
add("webdisplays.message.turnOn", "你需要先把屏幕打开！")
add("webdisplays.message.upgradeError", "升级失败 :（请查看日志……")
add("webdisplays.message.upgradeOk", "升级组件已安装！")
add("webdisplays.message.welcome1", "感谢你安装 WebDisplays！如果")
add("webdisplays.message.welcome2", "你需要帮助，把鼠标悬停在任意 WD 物品上")
add("webdisplays.message.welcome3", "然后按 F1。祝你玩得愉快 —— montoyo")

# Server block console
add("webdisplays.server.argerror", "无法识别的参数。")
add("webdisplays.server.error", "内部错误。请查看日志。")
add("webdisplays.server.error2", "内部错误 %d。请查看日志。")
add("webdisplays.server.errowner", "只有拥有者可以访问此项。")
add("webdisplays.server.fnamearg", "缺少文件名参数")
add("webdisplays.server.help.access", "§k没有帮助数据")
add("webdisplays.server.help.clear", "清屏")
add("webdisplays.server.help.exit", "退出此控制台")
add("webdisplays.server.help.help", "显示这段帮助")
add("webdisplays.server.help.ls", "列出本服务器上的文件")
add("webdisplays.server.help.owner", "显示服务器拥有者")
add("webdisplays.server.help.quota", "显示存储配额")
add("webdisplays.server.help.reconnect", "重新连接 MiniServ [调试]")
add("webdisplays.server.help.rm", "删除文件")
add("webdisplays.server.help.upload", "打开上传向导")
add("webdisplays.server.help.url", "把文件网址复制到剪贴板")
add("webdisplays.server.info", "需要帮助请输入 help。")
add("webdisplays.server.nameerr", "文件名无效")
add("webdisplays.server.notfound", "文件未找到")
add("webdisplays.server.ownername", "拥有者名称：%s")
add("webdisplays.server.owneruuid", "拥有者 UUID")
add("webdisplays.server.press_for_more", "<按向下键显示更多>")
add("webdisplays.server.queryerr", "查询错误，请尝试 reconnect。")
add("webdisplays.server.quota", "已使用 %s/%s")
add("webdisplays.server.timeout", "查询超时。请查看日志。")
add("webdisplays.server.unknowncmd", "未知命令。")
add("webdisplays.server.upload.done", "完成")
add("webdisplays.server.upload.exists", "错误：文件已存在")
add("webdisplays.server.upload.info", "选择要上传的文件")
add("webdisplays.server.upload.parent", "[上级目录]")
add("webdisplays.server.upload.quota", "错误：文件大小超出配额")
add("webdisplays.server.upload.uploading", "上传中……")
add("webdisplays.server.urlcopied", "网址已复制到剪贴板。")

# Sides
add("webdisplays.side.bottom", "底面")
add("webdisplays.side.east", "东")
add("webdisplays.side.north", "北")
add("webdisplays.side.south", "南")
add("webdisplays.side.top", "顶面")
add("webdisplays.side.west", "西")

out = collections.OrderedDict()
fallback = []
for key in en.keys():
    if key in zh:
        out[key] = zh[key]
    else:
        out[key] = en[key]
        fallback.append(key)

extra = [k for k in zh.keys() if k not in en]
for key in extra:
    out[key] = zh[key]

dst = os.path.join(LANG_DIR, "zh_cn.json")
with io.open(dst, "w", encoding="utf-8") as f:
    json.dump(out, f, ensure_ascii=False, indent=2)
    f.write("\n")

print("en keys: %d, translated: %d, fell back to English: %d, zh-only additions: %d"
      % (len(en), len(en) - len(fallback), len(fallback), len(extra)))
if fallback:
    print("fell back:", fallback)
