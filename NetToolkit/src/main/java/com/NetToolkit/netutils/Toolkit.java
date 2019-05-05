package com.NetToolkit.netutils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.NetToolkit.Global;

public class Toolkit {
	private static Logger log = Logger.getLogger("toolkit");
	
	/*
	 * DNSresolution  DNS解析有无结果返回
	 * @param domainName 下载地址
	 * @param List<String>ipsOfdomainName 存放解析出来的ip地址，可能有多个
	 * @return true表示域名可解析
	 *         false表示dns解析没有结果
	 */
	public static boolean isDNSResolvable(String domainName, List<String> ipsOfdomainName) {
		java.security.Security.setProperty("networkaddress.cache.ttl", "0"); // 设置不使用DNS缓存
		InetAddress[] addr1;
		try {
			addr1 = InetAddress.getAllByName(domainName);//.getByName(domainName)
			for(InetAddress ad: addr1){
				ipsOfdomainName.add(ad.getHostAddress());
			}
			return true;

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();		
		}
		return false;
	}
	
	/*
	 * 该DNS服务器能否正常解析常用域名
	 * @return true表示该DNS服务正常
	 * 		   false表示该DNS服务出现故障
	 */
	public static boolean isDNSServiceAvailable(){
		//读取常见域名
		String[] commonDomainNames = Global.commonDomainNameList.split(";");
		int ResolutionCnt = 0;
		for (String domainName: commonDomainNames){
			if (isDNSResolvable(domainName, new ArrayList<String>())){//
				ResolutionCnt += 1;
			}
		}
		//暂时的判定方法是：有一个常见域名可以解析，则认为DNS服务正常。。。此判定方法可能还需修改
		if (ResolutionCnt>0){
			//dns服务正常
			//System.out.println("所查询的网站域名未在DNS服务器中添加或更新");
			return true;
		}else{
			//System.out.println("DNS服务故障");
			return false;
		}
	}
	
	/* 
	 * 获取系统配置的DNS服务器
	 */
	public static String getSystemConfigDNSServer() {
		Pattern pstr = Pattern.compile(".*Address:\\s*(\\d+\\.\\d+\\.\\d+\\.\\d+).*");
		Matcher match = pstr.matcher(execCmd("nslookup localhost"));
		match.find();
		return match.group(1);
	}
	
	
	/*
	 * 通过网卡名称获取该网卡的mac地址
	 * @return 字符串：a0-b4-aa-54-c7-38
	 */
	public static String getMacAddressByName(String networkCardName) throws SocketException {
		NetworkInterface ni = NetworkInterface.getByName(networkCardName);
		if(ni==null){
			log.warning("没有名为"+networkCardName+"的网卡（网络接口）");
			return null;
		}
		byte[] mac = ni.getHardwareAddress();
		if(mac==null){
			log.warning("获取不到该网络接口的mac地址");
			return null;
		}
		//byte->int->16进制->string
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < mac.length; i++) {
			if (i != 0)
				sb.append("-");
			String tmp = Integer.toHexString(mac[i] & 0xFF);// 将byte转为正整数。然后转为16进制数
			sb.append(tmp.length() == 1 ? 0 + tmp : tmp);
		}
		return sb.toString().toLowerCase();
	}
	
	
	//获取本机IP地址
	public static String getLocalIPAddress() {
		String ip=null;
		for(Object ipAndGatewayObj : getIpAndGateway()){
			ip = ((JSONObject)ipAndGatewayObj).get("ip").toString();
			if(ip!=null)
				break;
		}
		return ip;
	}
	/*
	 * 通过路由表找到连接默认网关的ip接口，通过这些ip接口（InetAddress），
	 * 对应上他们所在的网络接口（NetworkInterface），然后获取网络接口的mac地址
	 * @return Set<String>：（a0-b4-aa-54-c7-38，a0-b4-aa-54-c7-39)
	 */
	public static Set<String> getLocalMacAddress() throws UnknownHostException, SocketException {
		Set<String> macSet = new HashSet<String>();

		for(Object ipAndGatewayObj : getIpAndGateway()){
			//String gateway = ((JSONObject)ipAndGatewayObj).get("gateway").toString();
			String ip = ((JSONObject)ipAndGatewayObj).get("ip").toString();
			System.out.println("ip="+ip);
			InetAddress ia = InetAddress.getByName(ip);
			NetworkInterface ni = NetworkInterface.getByInetAddress(ia);
			byte[] mac = ni.getHardwareAddress();
			//byte->int->16进制->string
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < mac.length; i++) {
				if (i != 0)
					sb.append("-");
				String tmp = Integer.toHexString(mac[i] & 0xFF);// 将byte转为正整数。然后转为16进制数
				sb.append(tmp.length() == 1 ? 0 + tmp : tmp);
			}
			//System.out.println("网络接口名称ni："+ni.getName()+"---"+"mac地址："+sb.toString().toLowerCase()+"---"+"ip地址："+ip);
			macSet.add(sb.toString().toLowerCase());
		}
		return macSet;

	}
	
	/*
	 * 通过路由表的目的网络是'0.0.0.0'获得网关
	 * @return List<JSONObject> (网关,ip)
	 */
	public static List getIpAndGateway(){
		String os = System.getProperty("os.name");
		List<JSONObject> netInfoList = new ArrayList<JSONObject>();
		if (os != null && os.startsWith("Windows")) {
			try{
				String command = "route print";
				Process p = Runtime.getRuntime().exec(command);
				BufferedReader br = new BufferedReader(new InputStreamReader(
						p.getInputStream(), "GBK"));
				String line;
				
				String[] tmp = null;
				JSONObject netInfo = null;
				
				Pattern pstr = Pattern.compile(".*(\\d+\\.\\d+\\.\\d+\\.\\d+).*");
				Matcher match = null;
				
				while ((line = br.readLine()) != null) {
					tmp = line.trim().split("\\s+");
					if (tmp.length > 0 && tmp[0].equals("0.0.0.0")) {
						match=pstr.matcher(tmp[3]);
						if(match.find()){
							netInfo = new JSONObject();
							netInfo.put("gateway", tmp[2]);
							netInfo.put("ip", tmp[3]);
							netInfoList.add(netInfo);
						}
					}

				}
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return netInfoList;
	}
	
	/*
	 * 通过traceroute的第一跳获得默认网关，然后获取本机的所有网络接口（NetworkInterface）以及每个网络接口的接口地址（InetAddress），
	 * 通过对比接口地址IP与网关是不是在同一个网段下(根据网络前缀即子网掩码来验证)，来判定这个接口地址ip所在的网络接口（NetworkInterface）是连接上网的那个接口，然后他的mac地址就是我们要找的
	 * @return Set<String>：（a0-ce-c8-09-c4-cb，0c-8b-fd-d5-1e-39)
	 */
	public static Set<String> getLocalMacAddress2() throws Exception{
		Set<String> macSet = new HashSet<String>();
		InetAddress ip = null;
		String gateway = getGateway();
		
		Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
		while(interfaces.hasMoreElements()){
			NetworkInterface ni = interfaces.nextElement();
			if(!ni.isVirtual() && !ni.isLoopback() && ni.isUp()){
				List<InterfaceAddress> infs = ni.getInterfaceAddresses();
				for(InterfaceAddress inf : infs){
					ip = inf.getAddress();
					if(ip!=null && ip instanceof Inet4Address && !ip.isLoopbackAddress()){
						if(ip.isSiteLocalAddress()){
							String ipStr = ip.getHostAddress();
							//获得网络前缀
							short maskLen = inf.getNetworkPrefixLength();
							//System.out.println("网络前缀："+maskLen);
							//System.out.println("gateway："+gateway);
							if(getNetworkNumber(ipStr, maskLen).equals(getNetworkNumber(gateway, maskLen))){
								//byte->int->16进制->string
								byte[] mac = ni.getHardwareAddress();
								StringBuffer sb = new StringBuffer();
								for(int i=0; i<mac.length; i++){
									if(i!=0)
										sb.append("-");
									String tmp = Integer.toHexString(mac[i]&0xFF);//将byte转为正整数。然后转为16进制数
									sb.append(tmp.length()==1?0+tmp:tmp);
								}
								//System.out.println("网络接口名称ni："+ni.getName()+"---"+"mac地址："+sb.toString().toLowerCase()+"---"+"ip地址："+ipStr);
								macSet.add(sb.toString().toLowerCase());
							}
						}	
					}
				}
			}
		}
		
		
		return macSet;
	}
	
	/*
	 * 获得某ip地址所在网络的网络号
	 * @param ip 
	 * @param masklen 掩码长度
	 * @return 网络号
	 */
	public static String getNetworkNumber(String ip, short masklen){
		String[] ipStr = ip.split("\\.");
		StringBuffer sb = new StringBuffer();
		sb.append(ipStr[0]);
		for(int i=1; i<ipStr.length; i++){
			if(i<masklen/8)
				sb.append("."+ipStr[i]);
			else
				sb.append(".0");
		}
		return sb.toString();
	}
	
	
	
	/*
	 * 通过traceroute的第一跳获得网关，缺点：没有连接网络时获取不到
	 * @return String 网关
	 */
	public static String getGateway(){
		String os = System.getProperty("os.name");
		
		if (os != null && os.startsWith("Windows")) {
			try{
				String command = "tracert -d www.baidu.com";
				Process p = Runtime.getRuntime().exec(command);
				BufferedReader br = new BufferedReader(new InputStreamReader(
						p.getInputStream()));
				String line;
				
				String[] tmp = null;
				while ((line = br.readLine()) != null){
					if(line.trim().startsWith("1")){
						tmp = line.trim().split("\\s+");
						if(tmp.length>0 && tmp[0].equals("1")){
							//System.out.println("网关："+tmp[tmp.length-1]);
							return tmp[tmp.length-1];
						}
					}
				}
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}
	
	
	/*
	 * ping ip 的状态
	 * 节点2-2.ping DNS服务器，看能否ping通
	 * @param ipAddress 
	 * @return true表示ip能ping通
	 *         false表示ip ping不通
	 * 节点8
	 */
	public static boolean getPingStatus(String ipAddress){
		String command = "ping -c 4 "+ipAddress;//linux
		Pattern pstr = Pattern.compile(".*,\\s*(\\d+)%\\s*packet loss.*");//linux

		String os = System.getProperty("os.name");
		if(os != null && os.startsWith("Windows")){
			command = "ping -n 4 "+ipAddress;
			pstr = Pattern.compile(".*\\((\\d+)%\\s*(丢失|loss).*");
		}
		try {
			Matcher match = pstr.matcher(execCmd(command));
			match.find();
			String packetLossRate = match.group(1);
			System.out.println("packetLossRate：" + packetLossRate + "%");
			if(Integer.parseInt(packetLossRate)<100){
				//丢包率<100，表示该主机可以ping通
				return true;
			}
		}catch (Exception e){
			log.info("没有找到匹配的项");
			e.printStackTrace();
		}

		return false;
	}
	
	public static boolean isTCPConnectionSucceeded(String host) {
		int port = 80;
		Socket so = null;
		boolean connectState = false;
		try {
			so = new Socket(host, port);
			log.info("连接成功！");
			connectState = true;
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.info("TCP连接失败！（unknown host）");
		} catch (ConnectException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.info("TCP连接失败！（Connection timed out）");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			if(so!=null){
				try {
					so.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
		return connectState;
	}
	
	
	/*
	 * 通w3c API获取时间和资源数据
	 * 注意：这里使用浏览器driver时，一定满足所安装的浏览器版本要求，否则会失败
	 * @param url 网址
	 * @param timingInfo 存放时间数据的容器
	 * @param resourcesInfo 存放资源数据的容器
	 * @return true表示成功采集
	 *         false表示采集失败
	 */
	public static boolean getTimingAndResourceInfo(String url, String userAgent, Map<String, Long> timingInfo, List  resourcesInfo){
		WebDriver driver = null;
		String os = System.getProperty("os.name");

		try{
			if(os != null && os.startsWith("Windows")) {
				if (userAgent.equalsIgnoreCase("chrome")) {
					//System.setProperty("webdriver.chrome.driver", Global.workSpaceDir+"thirdparty//chromedriver-v2.29-win32//chromedriver.exe");
					System.setProperty("webdriver.chrome.driver", Global.workSpaceDir + "/thirdparty/chromedriver-v2.34-win32/chromedriver.exe");
					driver = new ChromeDriver();
				} else if (userAgent.equalsIgnoreCase("firefox")) {
					System.setProperty("webdriver.gecko.driver", Global.workSpaceDir + "/thirdparty/geckodriver-v0.18.0-win32/geckodriver.exe");
					driver = new FirefoxDriver();
				} else if (userAgent.equalsIgnoreCase("internetexplorer")) {//只有windows有IE
					System.setProperty("webdriver.ie.driver", Global.workSpaceDir + "/thirdparty/IEDriverServer_Win32_3.5.0/IEDriverServer.exe");
					driver = new InternetExplorerDriver();
				}
			} else if(os != null && os.startsWith("Linux")) {
				if (userAgent.equalsIgnoreCase("chrome")) {
					System.setProperty("webdriver.chrome.driver", Global.workSpaceDir + "thirdparty//chromedriver-v2.34-linux64//chromedriver");
					driver = new ChromeDriver();
				} else if (userAgent.equalsIgnoreCase("firefox")) {
					System.setProperty("webdriver.gecko.driver", Global.workSpaceDir + "thirdparty//geckodriver-v0.18.0-linux64//geckodriver");
					driver = new FirefoxDriver();
				}
			}
			driver.manage().deleteAllCookies();//清除浏览器缓存//参考：http://www.cnblogs.com/111testing/p/6384628.html
			driver.manage().timeouts().pageLoadTimeout(300, TimeUnit.SECONDS);
			driver.get(url);
			System.out.println("final_url："+driver.getCurrentUrl());
			Global.full_url=driver.getCurrentUrl();/////重要，后面会用到

			JSONObject timingInfoTmp =  JSONObject.fromObject(
					((JavascriptExecutor)driver).executeScript("return JSON.stringify(window.performance.timing)")
			);
			for(Object key : timingInfoTmp.keySet()){
				//因为timingInfoTmp的value里有Integer又有Long
				timingInfo.put(key.toString(), Long.parseLong(timingInfoTmp.get(key).toString()));
			}
			
			List<Map<String, Object>> resourcesInfoTmp = (List<Map<String, Object>>) JSONArray.fromObject(
					((JavascriptExecutor)driver).executeScript("return window.performance.getEntriesByType(\"resource\")")
			);
			for(Map<String, Object> m : resourcesInfoTmp){
				resourcesInfo.add(m);
			}


		}catch(TimeoutException e){
			e.printStackTrace();
			System.out.println("采集超时");
			return false;
		}catch(Exception e){
			e.printStackTrace();
			log.warning("采集信息时出错");
			return false;
		}finally{
			if(driver!=null)
				driver.quit();
		}

		//System.out.println("测试timingInfo："+timingInfo);
		//System.out.println("测试resourcesInfo："+resourcesInfo);
		return true;
	}
	
	
	/*
	 * 网络拥塞
	 * @param domainName 网站域名
	 * @return 0表示没有出现拥塞
	 *         -1表示有丢包
	 *         -2表示延时大
	 */
	public static int isNetCongested(String domainName){
		String command = "ping -c 4 "+domainName;//linux
		Pattern pstr = Pattern.compile(".*,\\s*(\\d+)%\\s*packet loss.*");//linux
		
		String os = System.getProperty("os.name");
		if(os != null && os.startsWith("Windows")){
			command = "ping -n 4 "+domainName;
			pstr = Pattern.compile(".*\\((\\d+)%\\s*(丢失|loss).*");
		}
		String res = execCmd(command);
		 
		Matcher match = pstr.matcher(res);
		match.find();
		String packetLossRate = match.group(1);//丢包率
		System.out.println("packetLossRate："+packetLossRate+"%");
		if(Integer.parseInt(packetLossRate)>0){			
			//log.info("存在丢包情况，造成的原因主要有：物理线路故障、设备故障、病毒攻击、路由信息错误等");
			return -1;
		}else{//即使丢包率=0，也可能时延较大，网络拥塞
			int averageDelay = 0;
			if(os != null && os.startsWith("Windows")){
				Pattern pstr2 = Pattern.compile(".*(平均|Average)\\s*=\\s*(\\d+)ms.*");
				match = pstr2.matcher(res);
				match.find(); 
				averageDelay = Integer.parseInt(match.group(2));//丢包率
			}else if(os != null && os.startsWith("Linux")){
				Pattern pstr2 = Pattern.compile("\\d+(\\.\\d+)?/(\\d+(\\.\\d+)?)/\\d+(\\.\\d+)?");
				match = pstr2.matcher(res);
				match.find(); 
				averageDelay = (int)Float.parseFloat(match.group(2));//丢包率
			}
			
			
			log.info("当前平均时延--averageDelay："+averageDelay+"ms");
			if(averageDelay>Global.AVG_DELAY){
				//log.info("时延较大，可能出现网络拥塞");
				return -2;
			}else{
				//log.info("网络状况较好");
				return 0;
			}

		}
		
	}
	
	/*
	 * 分析资源文件是否下载成功，分析网页上是否有下载耗时的资源文件
	 * @param resourcesInfo
	 * @param log
	 * @param errObj
	 * @return true下载成功
	 * 		   false下载出错
	 */
	public static boolean downFileAnalysis(List<Map<String, Object>>  resourcesInfo, JSONObject errObj){
		//分析资源
		List<String> resourceUrls = new ArrayList<String>();
		for(int i=0; i<resourcesInfo.size(); i++){
			resourceUrls.add(resourcesInfo.get(i).get("name").toString());
		}
		log.info("开始分析资源文件下载信息--共"+resourceUrls.size()+"个资源");
		//System.out.println("测试resourceUrls的长度："+resourceUrls.size());
		for(int i=0; i<resourcesInfo.size(); i++){
			//对于每个资源文件，看他们是否存在资源文件过大，下载耗时长的情况，或者资源文件无法下载或访问
			execCurlDownload(resourceUrls.get(i), Global.workSpaceDir+"/downloadfiles/", errObj);
		}
		int failRate = (int)(errObj.size()*1.0/resourceUrls.size()*100);
		if (resourceUrls.size()==0 || failRate > 10) {
			// 有错误信息
			//log.info("网络故障--资源下载失败，"+ errObj.toString());
			return false;
		}
		log.info("资源下载成功率："+ (100-failRate)+"%");
		log.info("资源下载信息分析结束");
		return true;
	}
	
	
	/* 使用curl命令下载资源文件，并给出下载状态（成功或失败）
	 * @param url 下载地址
	 * @param savePath 下载保存路径，包含文件名称
	 * @param errObj 错误信息
	 */
	public static void execCurlDownload(String url, String savePath, JSONObject errObj){
		//url = "https://ss1.bdstatic.com/5eN1bjq8AAUYm2zgoY3K/r/www/cache/static/protocol/https/home/js/nu_instant_search_08089ad.js";
		String[] tmp = url.split("/");
		if(tmp[tmp.length - 1].contains("?"))//xx.js?wd=1  //xx?wd=1
			savePath = savePath + tmp[tmp.length - 1].split("\\?")[0];
		else{	
			savePath = savePath + tmp[tmp.length - 1];
		}
		// curl --connect-timeout 60 -m 600 -L -w stateCode=%{http_code}size=%{size_download}end -o D:/WAtests/nu_instant_search_08089ad.js https://ss1.bdstatic.com/5eN1bjq8AAUYm2zgoY3K/r/www/cache/static/protocol/https/home/js/nu_instant_search_08089ad.js
		String command = "curl" + " --connect-timeout "
						+ Global.CONNECT_TIMEOUT + " -m "
						+ Global.DOWNLOAD_TIMEOUT// -m/--max-time <seconds> 设置最大传输时间
						+ " -L"// 如果curl请求的地址产生重定向，那么使用-L参数会自动重定向
						+ " -w stateCode=%{http_code}size=%{size_download}end"// -w/write-out的作用就是输出点什么。curl的-w参数用于在一次完整且成功的操作后输出指定格式的内容到标准输出。
						// + " -A " + "\"" + userAgent + "\"" //-A/--user-agent <string>设置用户代理发送给服务器
						+ " -o " + savePath// -o/--output 把输出写到该文件中
						+ " " + url;
		String res = execCmd(command);

		// 检查
		if (res.contains("time out")) {
			errObj.put(url, "下载超时");
		}

		Pattern pstr = Pattern.compile(".*?stateCode=(\\d+)size=(.*?)end.*?");
		Matcher match = pstr.matcher(res);
		if (match.find()) {
			String stateCode = match.group(1);// 状态码
			String size = match.group(2);// 资源文件大小，单位是字节

			if ("200".equals(stateCode)) {
				File downFile = new File(savePath);
				if (downFile.exists()) {
					// 比较下载下来的文件个get请求返回的真实文件大小
					long filesize = Long.parseLong(size);
					if (filesize != downFile.length()) {
						log.info("文件下载中断，下载未完成！"+url);
						errObj.put(url, "文件下载中断，下载未完成！");
					}else{
						log.info("文件下载成功!"+url);
					} 
				} else {
					log.info("下载已完成，但文件丢失！"+url);
					errObj.put(url, "下载已完成，但文件丢失！");
				}
			} else {
				log.info("下载失败 +http_code:" + stateCode+"："+url);
				errObj.put(url, "下载失败 +http_code:" + stateCode+"："+url);
			}
		} else {
			log.info("下载返回结果中没有找到Http状态和文件大小"+url);
			errObj.put(url, "下载返回结果中没有找到Http状态和文件大小");
		}
	}
	
	
	/* 使用命令行操作，命令行的输出作为返回结果
	 * @param command 命令
	 * @return string 操作输出
	 */
	public static String execCmd(String command){
		//System.out.println(command);
		String os = System.getProperty("os.name");
		StringBuffer sb = new StringBuffer();
		if (os != null && os.startsWith("Windows")) {//应该在给出命令之前判断系统，因为命令可能不一样？
			try {
				Process p = Runtime.getRuntime().exec(command);			
				BufferedReader br = new BufferedReader(new InputStreamReader(
						p.getInputStream(), "GBK"));
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line+"\n");
				}
				br.close();
			}catch(IOException e){
				e.printStackTrace();
			}
		}//end if
		//System.out.println("测试命令行输出数据："+sb.toString());
		return sb.toString();
	}
	
}
