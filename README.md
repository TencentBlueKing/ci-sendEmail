# 发送邮件通知插件

## 功能说明
调用蓝鲸ESB发送邮件

## 配置
插件上架时，需要配置蓝鲸ESB相关参数，路径：设置->私有配置
- bk_app_code: ESB appcode // 如bk_ci等已在蓝鲸内注册的app
- bk_app_secret: ESB appsecret // 对应app的密钥
- bk_host: ESB host // 当前环境蓝鲸域名
- bk_username : ESB username // 蓝鲸内用户名，可为“admin”
- sender: 邮件发送人 // 需在 开发者中心-API网关-通道管理-选择系统CMSI-搜索 发送邮件 配置相关信息。
  smtpPwd: smtp用户对应授权码。(以QQ邮箱为例：设置-> 账户-> POP3/IMAP/SMTP/Exchange/CardDAV/CalDAV服务->生成授权码) 


## change log

### v1.0.3
fix: 敏感信息修复 #27

### v1.0.4
fix: 某些网络情况下会导致发送卡住 #30
