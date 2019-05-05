package com.NetToolkit;

public class Global {
	
	//常见网站域名
	public static String commonDomainNameList = "www.baidu.com;www.taobao.com;www.sina.com.cn;www.sohu.com;www.csdn.net;www.jd.com";//系统配置的DNS服务器
	public static String full_url=null;
	public static String workSpaceDir=System.getProperty("user.dir");
	
	public static int AVG_DELAY=50;//平均时延(单位：ms)，用于ping的平均时延的比较
	public static int CONNECT_TIMEOUT=10;//连接超时时间(单位：s)，用于curl命令
	public static int DOWNLOAD_TIMEOUT=20;//下载超时时间(单位：s)，用于curl命令
	
	/*
	 * authorityDNSServers=8.8.8.8;8.8.4.4
#
CONNECT_TIMEOUT=10
#下载超时时间(单位：s)，用于curl命令
DOWNLOAD_TIMEOUT=20
#平均时延(单位：ms)，用于ping的平均时延的比较
AVG_DELAY=50
#DNS解析时间上限:5s*1000
#windows默认是5s，nslook默认是2s，参考http://bbs.chinaunix.net/thread-937657-1-1.html
DNSRESOLVE_TIMEOUT=2000

#平均连接时间
#CONNECT_TIME_AVG=5000
#平均DNS解析时间
#DNSRESOLVE_TIME_AVG=1000

	 */
}
