package org.apache.flume.source.rocketmq;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.StringUtils;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * @Company: ly.com
 * @Project: DataCleanService
 * @Desc:    desc
 * @Todo:	 TODO
 * @Author:  lzy12173
 * @Date:    2015-8-31下午3:17:24
 */
public class CommonUtils {
	private final static String NULL_DEFAULT = "null" ;

	//final static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.CHINA);

	//to convert timestamp to long
	public static long convertTSToLong(String ts){
		long tsLong = 0;
		try{
			tsLong = Timestamp.valueOf(ts).getTime();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return tsLong ;
	}
	
	//to convert long to timestamp
	public static String convertLongTsToString(long longTs){
		return new Timestamp(longTs).toString();
	}
	
	public static boolean isBadJson(String json) {  
        return !isGoodJson(json);  
    }  
  
    public static boolean isGoodJson(String json) {  
        if (StringUtils.isBlank(json)) {
            return false;  
        }  
        try {  
            JSONObject.parse(json);
            return true;  
        } catch (Exception e) {
        	System.err.println("bad json: " + json);  
            return false;  
        }  
    }


	/**
	 * get calendar instance.
	 * @param day
	 * @return
     */
	public static Calendar getCalendarInstance(int day){

		Calendar calendar = Calendar.getInstance(); //得到日历
		calendar.setTime(new Date());//把当前时间赋给日历
		calendar.setTimeZone(TimeZone.getTimeZone("GMT+8"));
		calendar.add(Calendar.DAY_OF_MONTH, day);  //设置为前一天

		return calendar ;
	}


	public static boolean isNullValue (Object obj){

		boolean isNull = false;
		//null
		if( null == obj ){ return true; }

		String val = obj.toString().trim() ;
		// " "
		if(StringUtils.isEmpty(val)){ return true; }
		//'null'
		if(NULL_DEFAULT.equals(val.toLowerCase())){
			return true;
		}

		return  isNull;

	}

	public static void main(String[] args){


		String ss = "{\"Id\":0,\"ServiceName\":\"\",\"CatType\":\"Frontend\",\"SubCatType\":\"ChuJingYou\",\"Account\":null,\"RequestParams\":\"\",\n" +
				"\"RequestTime\":\"2016-02-02T10:30:29.0017565+08:00\",\"SpentTimes\":0,\"RemoteMachineName\":\"172.16.3.220\",\n" +
				"\"ResponseCode\":\"\",\"ResponseContent\":\"\",\"LogLevel\":0,\n" +
				"\"RequestCount\":0,\"RequestId\":\"\",\"DllVersion\":\"1.1.0.25688\",\"RemoteIp\":null,\"CatTypeString\":\"\",\"SubCatTypeString\":\"\",\"ProjectCode\":\"ivcwa\",\"AssemblyName\":\"TC.InterVacation.Components.dll\",\"Namespace\":\"TC.InterVacation.Components.Diagnostics.Log\",\"Class\":\"Logger\",\"Method\":\"Error\",\"ClientIp\":null,\"DeployIp\":\"172.16.3.220\",\"InvokeTime\":\"2016-02-02T10:30:29.0027566+08:00\",\"RequestIdentify\":\"f3b38d95-2024-42ac-bd6f-291b66a290bc\",\"RequestKey\":\"f3b38d95-2024-42ac-bd6f-291b66a290bc\",\"ESTimeString\":\"2016-02-02T10:30:29.002Z\",\"LogUid\":\"b029fdbd-4a41-4ad0-b59a-22ad096ac382\",\"LogSize\":1814,\"LogSource\":0,\"ExpectStoreDays\":0}";





		System.out.println("check is null:"+isGoodJson(ss));


		

	}
	
}

