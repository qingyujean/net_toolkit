package com.NetToolkit.ipip;

import java.io.IOException;
import java.util.Arrays;

import com.NetToolkit.ipip.datx.BaseStation;
import com.NetToolkit.ipip.datx.City;
import com.NetToolkit.ipip.datx.District;
import com.NetToolkit.ipip.datx.IPv4FormatException;

public class TestCity {

	public static void main(String[] args) {
		 try {
			 String datx_path = System.getProperty("user.dir")+"/17mon/";
			 //含运营商信息
			 City city = new City(datx_path+"mydata4vipweek2.datx"); // 城市库
			 //不含运营商信息
			 //City city = new City(datx_path+"17monipdb1/17monipdb.datx");			 	
			 System.out.println(Arrays.toString(city.find("180.97.33.108")));
			 /*
			 District district = new District("/path/to/quxian.datx");//区县库

			 System.out.println(Arrays.toString(district.find("1.12.0.0")));
			 System.out.println(Arrays.toString(district.find("223.255.127.250")));

			 BaseStation baseStation = new BaseStation("/path/to/station_ip.datx"); // 基站库
			 System.out.println(Arrays.toString(baseStation.find("8.8.8.8")));
			 System.out.println(Arrays.toString(baseStation.find("223.221.121.0")));
			 */
	    } catch (IOException ioex) {
	        ioex.printStackTrace();
	    } catch (IPv4FormatException ipex) {
	        ipex.printStackTrace();
	    }
	}

}
