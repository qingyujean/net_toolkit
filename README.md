# net_toolkit
Java封装的网络测试工具集，集成了nslookup、ping、curl、traceroot等多个小工具，可用于追溯访问一个URL的整个网络过程，访问网站URL使用扩展WebDriver Selenium以及基于W3C标准的Resource Timing API和Navigation Timing API

文件入口：MainTest

我的java运行环境是：java version 1.8.0_144

说明：MainTest中有调用工具集中方法的样例，主要包含以下内容：
测试1：获取系统配置的DNS服务器
测试2：获取本机IP（能上外网）地址
测试3：获取本机配置的网关
测试4：获取本机mac地址
测试5：获取域名解析结果
测试6：配置的域名服务器是否能正常提供服务
测试7：TCP连接是否成功；前提：执行测试5
测试8：ping网站服务器，看是否能ping通；前提：执行测试5
测试9：获取traceroute 信息
测试10：网站是否使用了HTTPS
测试11：如果使用了HTTPS，SSL握手是否成功；前提：执行测试10
测试12：HTTP是否有响应返回；前提：执行测试10
测试13：检查丢包率以了解网络拥塞情况
测试14：下载网站页面，当下载完成之后，分析是否有大资源或打开失败的外链；前提：执行测试10
