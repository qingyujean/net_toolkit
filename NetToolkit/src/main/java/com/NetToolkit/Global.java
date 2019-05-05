package com.NetToolkit;

public class Global {
	
	//常见网站域名
	public static String commonDomainNameList = "www.baidu.com;www.taobao.com;www.sina.com.cn;www.sohu.com;www.csdn.net;www.jd.com";//系统配置的DNS服务器
	public static String full_url=null;
	public static String workSpaceDir=System.getProperty("user.dir");
	
	public static int AVG_DELAY=50;//平均时延(单位：ms)，用于ping的平均时延的比较
	public static int CONNECT_TIMEOUT=10;//连接超时时间(单位：s)，用于curl命令
	public static int DOWNLOAD_TIMEOUT=20;//下载超时时间(单位：s)，用于curl命令
	
}
