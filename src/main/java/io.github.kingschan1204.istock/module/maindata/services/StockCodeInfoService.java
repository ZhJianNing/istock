package io.github.kingschan1204.istock.module.maindata.services;

import com.alibaba.fastjson.JSONArray;
import io.github.kingschan1204.istock.module.spider.openapi.TushareApi;
import io.github.kingschan1204.istock.module.maindata.po.StockCodeInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 代码信息服务类
 * @author chenguoxiang
 * @create 2018-10-30 15:22
 **/
@Slf4j
@Service
public class StockCodeInfoService {
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private TushareApi tushareSpider;

    private static List<String>  zixuan  = new ArrayList<>();


    static {
        zixuan.add("000338");zixuan.add("000581");zixuan.add("000625");zixuan.add("000799");zixuan.add("000937");
        zixuan.add("002013");zixuan.add("002077");zixuan.add("002216");zixuan.add("002265");zixuan.add("002426");
        zixuan.add("002455");zixuan.add("002459");zixuan.add("002594");zixuan.add("002714");zixuan.add("003025");
        zixuan.add("600367");zixuan.add("600418");
        zixuan.add("600436");zixuan.add("600519");zixuan.add("600522");zixuan.add("600660");zixuan.add("600685");
        zixuan.add("600764");zixuan.add("600860");zixuan.add("600905");zixuan.add("601127");zixuan.add("601633");
        zixuan.add("603005");zixuan.add("603530");zixuan.add("603630");zixuan.add("605337");

    }


    /**
     * 代码列表刷新
     */
    public void refreshCode(){
        List<StockCodeInfo> list = new ArrayList<StockCodeInfo>();
        JSONArray rows =tushareSpider.getStockCodeList();
        for (int i = 0; i < rows.size(); i++) {
            //这里只筛选自己自选的股票
            StockCodeInfo stockCodeInfo = new StockCodeInfo(rows.getJSONArray(i));
            if(isZiXuan(stockCodeInfo)){
                list.add(stockCodeInfo);
            }
        }
        //先删除再新增
        mongoTemplate.dropCollection(StockCodeInfo.class);
        mongoTemplate.insertAll(list);
    }

    private boolean isZiXuan(StockCodeInfo stockCodeInfo){
        if (zixuan.contains(stockCodeInfo.getCode())){
            return true;
        }
        return false;
    }

    /**
     * 返回所有代码
     *
     * @return
     */
    public List<StockCodeInfo> getAllStockCodes() {
        return mongoTemplate.find(new Query(), StockCodeInfo.class);

    }

    /**
     * 返回沪市代码
     * @return
     */
    public List<StockCodeInfo> getSHStockCodes() {
        return mongoTemplate.find(new Query(Criteria.where("type").is("sh")), StockCodeInfo.class);

    }

    /**
     * 返回深市代码
     * @return
     */
    public List<StockCodeInfo> getSZStockCodes() {
        return mongoTemplate.find(new Query(Criteria.where("type").is("sz")), StockCodeInfo.class);
    }
}
