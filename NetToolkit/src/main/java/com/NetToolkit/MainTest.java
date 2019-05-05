package com.NetToolkit;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import net.sf.json.JSONObject;

import org.apache.http.HttpEntity;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.NetToolkit.ipip.datx.City;
import com.NetToolkit.ipip.datx.IPv4FormatException;
import com.NetToolkit.netutils.Toolkit;
import com.NetToolkit.netutils.ssl.SslUtil;

/**
 * Hello world!
 *
 */
public class MainTest 
{
	private static Logger log = Logger.getLogger("pullTask");
	private static int statusCode = 0;//
	private static String reasonPhrase = "";//
	private static List<String> ipsOfdomainName = new ArrayList<String>();//该域名的解析结果
	private static Map<String, Long> timingInfo = new HashMap<String, Long>();
	private static List<Map<String, Object>>  resourcesInfo = new ArrayList();
	public static String URL = null;
	
    public static void main( String[] args )
    {
        URL="www.baidu.com";
        Global.full_url="http://"+URL;
        
        Boolean res = false;
        String os = System.getProperty("os.name");
        String extra_info="";
        
        
        //测试1：获取系统配置的DNS服务器
        String dnsServer = Toolkit.getSystemConfigDNSServer();
        System.out.println("系统配置的DNS服务器是："+dnsServer);
        
        /*
        //测试2：获取本机IP（能上外网）地址
        String ip = Toolkit.getLocalIPAddress();
        System.out.println("本机IP是："+ip);
        
        
        //测试3：获取本机配置的网关
        String gateway = Toolkit.getGateway();
        System.out.println("配置的网关是："+gateway);
        
        
        //测试4：获取本机mac地址
        String macAddress = null;
        try{
			if(os != null && os.startsWith("Windows")){
				Set<String> macSet = Toolkit.getLocalMacAddress();//使用route print
				//Set<String> macSet = Toolkit.getLocalMacAddress2();//使用traceroute
				macAddress = macSet.toArray()[0].toString();
			}else if(os != null && os.startsWith("Linux")){
				//通过网卡名字获知mac地址
				macAddress = Toolkit.getMacAddressByName("eth0");//eth0
			}
			if(macAddress==null){
    			log.severe("mac地址获取失败，无法完成注册");
    		}else{
    			System.out.println("获取mac地址："+macAddress);
    		}
        }catch (Exception e){
    		log.severe("mac地址获取失败，无法完成注册");
        }
       
        
        //测试5：获取域名解析结果
        res = Toolkit.isDNSResolvable(URL, ipsOfdomainName);
        if(res)
        	System.out.println(URL+"解析出的IP为："+ipsOfdomainName);
        else
        	System.out.println(URL+"域名解析失败");
        
        
        //测试6：配置的域名服务器是否能正常提供服务
        res=Toolkit.isDNSServiceAvailable();
        if(res)
        	System.out.println("DNS服务正常");
        else
        	System.out.println("DNS服务故障");
        
        
        //测试7：TCP连接是否成功；前提：执行测试5
        res=Toolkit.isTCPConnectionSucceeded(ipsOfdomainName.get(0));
        if(res)
        	System.out.println("TCP连接成功");
        else
        	System.out.println("TCP连接失败");
        
        
        
      	//测试8：ping网站服务器，看是否能ping通；前提：执行测试5
        res=Toolkit.getPingStatus(ipsOfdomainName.get(0));
        if(res)
        	System.out.println(ipsOfdomainName.get(0)+"服务器可达");
        else
        	System.out.println(ipsOfdomainName.get(0)+"服务器不可达");
        
        
        //测试9：获取traceroute 信息
        String command = "traceroute "+URL;//linux不加-d参数
		if(os != null && os.startsWith("Windows")){
			command = "tracert -d "+URL;
		}
		String traceroute_info = Toolkit.execCmd(command);
		log.info("诊断节点8的extra_info："+traceroute_info);
		
		
		
		//测试10：网站是否使用了HTTPS
        boolean collectState = Toolkit.getTimingAndResourceInfo(Global.full_url, "Chrome", timingInfo, resourcesInfo);
		log.info("timingInfo:"+timingInfo);
		log.info("resourcesInfo:"+resourcesInfo);
		boolean isHttps = Global.full_url.split(":")[0].equals("https");
		if(isHttps){			
			log.info("网站使用了HTTPS"); 
		}else{
			log.info("网站未使用HTTPS");
		}
		
		
		
		//测试11：如果使用了HTTPS，SSL握手是否成功；前提：执行测试10
		if(isHttps){
			if(!isSSLHandshakeSucceeded()){
				log.info("SSL握手失败");
			}else{
				log.info("SSL握手成功"); 
			}
		}
		
		
		
		//测试12：HTTP是否有响应返回；前提：执行测试10
		boolean httpRes = httpHasResponse(Global.full_url);
		if(httpRes){
			log.info("http有响应返回");
		}else{
			log.info("http无响应返回");
		}
		//HTTP请求状态码
		if(statusCode!=200){
			//返回不成功。诊断结束
			log.info("http请求失败，返回错误代码"+statusCode);
			log.info( statusCode+": "+reasonPhrase);
		}else{
			log.info("http请求成功，返回200");
		}
		
		
		
		//测试13：检查丢包率以了解网络拥塞情况
		int result = Toolkit.isNetCongested(URL);
		
		if(result == -1){
			//有丢包
			extra_info = "存在丢包情况，造成的原因主要有：物理线路故障、设备故障、病毒攻击、路由信息错误等";
		}else if(result == -2){
			//延时大
			extra_info = "时延较大，可能出现网络拥塞";
		}else{
			//网络状况较好
			extra_info = "网络状况较好";
		}
		System.out.println(extra_info);
		
		
		//测试14：下载网站页面，当下载完成之后，分析是否有大资源或打开失败的外链；前提：执行测试10
        JSONObject errObj = new JSONObject();
        if(Toolkit.downFileAnalysis(resourcesInfo, errObj)==false){
			//有下载出错信息
			extra_info = errObj.toString();
			log.info("无严重故障，网站页面可能存在资源加载超时或失败："+extra_info);
		}else{
			//下载信息没错
			log.info("网站访问正常");
		}
		
        
        //测试15：获取IP的定位信息（可定位境内外IP）；前提：执行测试5
        String datx_path = System.getProperty("user.dir")+"/17mon/";
        try {
	        //含运营商信息
	        City city = new City(datx_path+"mydata4vipweek2.datx"); // 城市库
			//不含运营商信息
			//City city = new City(datx_path+"17monipdb1/17monipdb.datx");			 	
			System.out.println(Arrays.toString(city.find(ipsOfdomainName.get(0))));
        } catch (IOException ioex) {
	        ioex.printStackTrace();
	    } catch (IPv4FormatException ipex) {
	        ipex.printStackTrace();
	    }
	    
        
        //测试16：获取IP的定位信息的其他接口（只能定位国内IP）；前提：执行测试5
        String ipLoate = getIpLocation2(ipsOfdomainName.get(0));
        System.out.println(ipLoate);
        */
    }
    
    //SSL握手是否成功
 	public static boolean isSSLHandshakeSucceeded() {	
 		try{
 			//火狐浏览器没有secureConnectionStart字段
 			if (timingInfo.containsKey("secureConnectionStart") && (timingInfo.get("secureConnectionStart")).longValue()== 0) {
 				return false;
 			}
 			
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 		return true;
 	}
 	
 	/*
	 * HTTP有无应答返回
	 * @param url string
	 * @return true HTTP有应答
	 * 		   false HTTP无应答
	 */
	public static boolean httpHasResponse(String url) {
		//System.setProperty("jsse.enableSNIExtension", "false");
		boolean status = true;
		
		if(url.split(":")[0].equals("https")){
			System.setProperty("jsse.enableSNIExtension", "false");//不加这一句会报错：javax.net.ssl.SSLProtocolException: handshake alert:  unrecognized_name
	        HttpURLConnection httpcon = null;
	        try {
	        	SslUtil.ignoreSsl();
	            java.net.URL u = new java.net.URL(url);
	            httpcon = (HttpURLConnection) u.openConnection();
	            httpcon.setRequestProperty("user-agent","Mozilla/5.0 (Windows NT 10.0; WOW64; rv:46.0) Gecko/20100101 Firefox/46.0"); 
	            httpcon.connect();
	            statusCode = httpcon.getResponseCode();
	            reasonPhrase = httpcon.getResponseMessage();
			}  catch (SocketException e){
				log.info("Recv failure: Connection was aborted");
				e.printStackTrace();
				status = false;
				reasonPhrase = "Connection was aborted";
			} catch (NoHttpResponseException e){
				log.info(URL + "未发送任何数据响应：Http has no response");
				e.printStackTrace();
				status = false;
				reasonPhrase = URL + "未发送任何数据响应：Http has no response";
			} catch (Exception e) {
	        	log.info("http response:"+statusCode+", "+reasonPhrase);
	            e.printStackTrace();
	            status = false;
	        }
			
		} else {
		
			
			CloseableHttpClient httpclient = HttpClients.createDefault();
			// 测试：一个返回404的页面：www.zhihu.com/question/abcde
			// HttpGet("http://"+"www.zhihu.com/question/abcde");
			HttpGet httpget = new HttpGet(url);
			// 模拟浏览器访问
			httpget.setHeader("User-Agent",
					"Mozilla/5.0 (Windows NT 10.0; WOW64; rv:46.0) Gecko/20100101 Firefox/46.0");
			// HttpGet httpget = new HttpGet("http://www.google.com");//用于测试
			CloseableHttpResponse response = null;
			try {
				response = httpclient.execute(httpget);
				// HttpEntity entity = response.getEntity();
				statusCode = response.getStatusLine().getStatusCode();
				reasonPhrase = response.getStatusLine().getReasonPhrase();
				// System.out.println(statusCode);
				// System.out.println(reasonPhrase);
			} catch (SocketException e){
				log.info("Recv failure: Connection was aborted");
				e.printStackTrace();
				status = false;
				reasonPhrase = "Connection was aborted";
			} catch (NoHttpResponseException e){
				log.info(URL + "未发送任何数据响应：Http has no response");
				e.printStackTrace();
				status = false;
				reasonPhrase = URL + "未发送任何数据响应：Http has no response";
			} catch (Exception e) {
				// TODO Auto-generated catch block
				log.info("http response:" + response);
				e.printStackTrace();
				status = false;
			} finally {
				try {
					if (response != null)
						response.close();
					httpclient.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
		System.out.println("http response:"+statusCode+", "+reasonPhrase);
		return status;
	}
	
	
	

    //百度地图接口
    public static String getIpLocation2(String ip){
        String url = "http://api.map.baidu.com/location/ip?ip="+ip+"&ak=OrrREGhqUN4ShFcIHiIZZT6F&coor=bd09ll";
        //System.out.println(url);
        String resp = sendGet(url);
        //System.out.println("resp="+resp);
        if(resp=="")
            return "";

        JSONObject respObj = JSONObject.fromObject(((JSONObject)JSONObject.fromObject(resp).get("content")).get("address_detail"));

        return respObj.get("province")+"-"+respObj.get("city");
    }
	
  //不带参数的get请求
    public static String sendGet(String get_url){
        CloseableHttpClient httpclient = HttpClients.createDefault();
        //System.out.println("测试get_url："+get_url);
        HttpGet httpget = new HttpGet(get_url);//模拟浏览器访问
        httpget.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:46.0) Gecko/20100101 Firefox/46.0");

        CloseableHttpResponse response = null;
        String response_contents = "";
        try {
            response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();
            //System.out.println(response.getStatusLine());  //响应状态
            if (entity != null) {
                //System.out.println("--------------------------------------");
                // 打印响应内容长度
                //System.out.println("Response content length: " + entity.getContentLength());
                // 打印响应内容
                response_contents = EntityUtils.toString(entity);
                //System.out.println("Response content: " + response_contents);
                //System.out.println("--------------------------------------");
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println("超时！");
            return response_contents;
        }finally{
            try {
                response.close();
                httpclient.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return response_contents;
            }
        }
        return response_contents;
    }
}
