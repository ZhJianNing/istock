package io.github.kingschan1204.istock.module.spider.openapi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.github.kingschan1204.istock.module.spider.util.TradingDateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * 用挖地免的接口
 *
 * @author chenguoxiang
 * @create 2018-10-30 9:31
 **/
//@RefreshScope
@Slf4j
@Component("TushareSpider")
public class TushareApi {

    @Value("${tushare.token}")
    private String tuToken = "66cdab7a757dcb728f8833f732a928791d67b6f38e1cd8d94bb79a0d";
    @Autowired
    private RestTemplate restTemplate = new RestTemplate();
    final String api = "http://api.tushare.pro";

    /**
     * 格式化成tushare API所需格式
     *
     * @param code
     * @return
     */
    public static String formatCode(String code) {
        //5开头，沪市基金或权证 60开头上证
        if (code.matches("^60.*|^68.*|^5.*")) {
            return String.format("%s.SH", code);
        }
        //1开头的，是深市基金 00开头是深圳
        else if (code.matches("^1.*|^00.*|^300...")) {
            return String.format("%s.SZ", code);
        }
        return null;
    }

    /**
     * post 方式提交
     *
     * @param params
     * @return
     */
    String post(JSONObject params) {
        HttpHeaders headers = new HttpHeaders();
        //定义请求参数类型，这里用json所以是MediaType.APPLICATION_JSON
        headers.setContentType(MediaType.APPLICATION_JSON);
        params.put("token", tuToken);
        HttpEntity<String> formEntity = new HttpEntity<String>(params.toString(), headers);
        String result = restTemplate.postForObject(api, formEntity, String.class);
        return result;
    }


    /**
     * 返回已上市的A股代码
     *
     * @return TS代码, 股票代码, 股票名称, 所在地域, 所属行业, 股票全称, 市场类型 （主板/中小板/创业板）,上市状态： L上市 D退市 P暂停上市,上市日期
     */
    public JSONArray getStockCodeList() {
        JSONObject json = new JSONObject();
        //接口名称
        json.put("api_name", "stock_basic");
        //只取上市的
        json.put("params", JSON.parse("{'list_status':'L'}"));
        json.put("fields", "ts_code,symbol,name,area,industry,fullname,market,list_status,list_date");
        String result = post(json);
        log.info("获取代码列表：{}", result);
        JSONObject datas = JSON.parseObject(result);
        JSONArray items = datas.getJSONObject("data").getJSONArray("items");
        return items;
    }

    /**
     * 获取上海公司基础信息
     *
     * @return
     */
    public JSONArray getStockShCompany() {
        JSONObject json = new JSONObject();
        //接口名称
        json.put("api_name", "stock_company");
        json.put("params", JSON.parse("{'exchange':'SSE'}"));
        json.put("fields", "ts_code,chairman,manager,secretary,reg_capital,setup_date,province,city,introduction,website,email,office,employees,main_business,business_scope");
        String result = post(json);
        JSONObject data = JSON.parseObject(result);
        JSONArray items = data.getJSONObject("data").getJSONArray("items");
        return items;
    }

    /**
     * 获取深圳公司基础信息
     *
     * @return
     */
    public JSONArray getStockSZCompany() {
        JSONObject json = new JSONObject();
        //接口名称
        json.put("api_name", "stock_company");
        json.put("params", JSON.parse("{'exchange':'SZSE'}"));
        json.put("fields", "ts_code,chairman,manager,secretary,reg_capital,setup_date,province,city,introduction,website,email,office,employees,main_business,business_scope");
        String result = post(json);
        JSONObject data = JSON.parseObject(result);
        JSONArray items = data.getJSONObject("data").getJSONArray("items");
        return items;
    }

    /**
     * 得到前10大持有人
     * 2021-12-14 16:27:07.368 ERROR 38392 --- [      前十大股东-t-2] i.g.k.i.m.spider.openapi.TushareApi      : 解析返
     * 回数据错误：datas={"msg":"抱歉，您每分钟最多访问该接口10次，权限的具体详情访问：https://tushare.pro/document/1?doc_id=108。",
     * "code":40203,"request_id":"aa2dd4f65cb711ecb5febb700d8b92fc1639470444554397"}
     *
     * @param code
     * @return
     */
    public JSONArray getStockTopHolders(String code) {
        JSONObject json = new JSONObject();
        //接口名称
        json.put("api_name", "top10_holders");
        json.put("params", JSON.parse(String.format("{'ts_code':'%s'}", code)));
        json.put("fields", "ts_code,ann_date,end_date,holder_name,hold_amount,hold_ratio");
        String result = post(json);
        JSONObject datas = JSON.parseObject(result);
        JSONArray items = null;
        try{
            items = datas.getJSONObject("data").getJSONArray("items");
        }catch (NullPointerException e){
            log.error("解析返回数据错误：datas={}",datas);
        }
//        JSONArray items = datas.getJSONObject("data").getJSONArray("items");
        return items;
    }

    /**
     * 股东人数
     *
     * @param code       代码
     * @param start_date 开始日期
     * @param end_date   结束日期
     * @return
     */
    public JSONArray getStockHolderNumber(String code, String start_date, String end_date) {
        JSONObject json = new JSONObject();
        JSONObject params = new JSONObject();
        params.put("ts_code", code);
        params.put("start_date", start_date);
        params.put("end_date", end_date);
        //接口名称
        json.put("api_name", "stk_holdernumber");
        json.put("params", params);
        String result = post(json);
        JSONObject datas = JSON.parseObject(result);
        JSONArray items = datas.getJSONObject("data").getJSONArray("items");
        return items;
    }

    /**
     * 日线行情
     *
     * @param code       代码
     * @param start_date 开始日期
     * @param end_date   结束日期
     * @return
     */
    public JSONArray getStockDailyPrice(String code, String start_date, String end_date) {
        JSONObject json = new JSONObject();
        JSONObject params = new JSONObject();
        params.put("ts_code", code);
        params.put("start_date", start_date);
        params.put("end_date", end_date);
        //接口名称
        json.put("api_name", "daily");
        json.put("params", params);
        String result = post(json);
        JSONObject datas = JSON.parseObject(result);
        JSONArray items = datas.getJSONObject("data").getJSONArray("items");
        return items;
    }

    /**
     * 每日指标基本面
     *
     * @param code       代码
     * @param start_date 开始日期
     * @param end_date   结束日期
     * @return
     */
    public JSONArray getStockDailyBasic(String code, String start_date, String end_date) {
        JSONObject json = new JSONObject();
        JSONObject params = new JSONObject();
        params.put("ts_code", code);
        params.put("start_date", start_date);
        params.put("end_date", end_date);
        //接口名称
        json.put("api_name", "daily_basic");
        json.put("params", params);
        String result = post(json);
        log.info("股票日常指标抓任务【Tushare返回】{}",result);
        //这里result返回：
//        {
//            "request_id": "c2e0716c5cf711ec9b11df5169a2431e1639497973780957",
//                "code": 40203,
//                "msg": "抱歉，您没有访问该接口的权限，权限的具体详情访问：https://tushare.pro/document/1?doc_id=108。",
//                "data": null
//        }
        //每日指标数据	daily_basic	交易日每日15点～17点之间更新	600起
        //需要600积分，暂时先不执行该任务
        JSONObject datas = JSON.parseObject(result);
        JSONArray items = datas.getJSONObject("data").getJSONArray("items");
        return items;
    }

    /**
     * https://tushare.pro/document/2?doc_id=128
     *
     * @param date
     * @return
     */
    public JSONArray index_dailybasic(String date) {
        JSONObject json = new JSONObject();
        JSONObject params = new JSONObject();
        params.put("trade_date", date);
        //接口名称
        json.put("api_name", "index_dailybasic");
        json.put("params", params);
        json.put("fields", "ts_code,trade_date,turnover_rate,turnover_rate_f,pe,pe_ttm,pb");
        String result = post(json);
        JSONObject datas = JSON.parseObject(result);
        JSONArray items = datas.getJSONObject("data").getJSONArray("items");
        return items;
    }

    public static void main(String[] args) {
       /* try {
            //{"date":"2018-11-01 18:06:25","code":200,"address":"湖南省长沙市 电信","ip":"113.246.64.67"}
            System.setProperty("https.maxRedirects", "50");
            System.getProperties().setProperty("https.proxySet", "true");
            System.getProperties().setProperty("https.proxyHost", "116.209.55.238");
            System.getProperties().setProperty("https.proxyPort", "9999");
            StockSpider.enableSSLSocket();
            String json =Jsoup.connect("https://www.ipip.net/").get().text();
            System.out.println(json);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }*/
        TradingDateUtil tradingDateUtil = new TradingDateUtil();
//        String startDate = tradingDateUtil.minusDate(10, 0, 0, "yyyyMMdd");
        JSONArray data = new TushareApi().index_dailybasic("20200205");
        for (int i = 0; i < data.size(); i++) {
            JSONArray ja = data.getJSONArray(i);
            System.out.println(ja);
        }
    }
}
