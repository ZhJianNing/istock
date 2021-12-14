package io.github.kingschan1204.istock.module.spider.timerjob;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.github.kingschan1204.istock.module.spider.timerjob.impl.*;

import java.util.HashMap;

/**
 * 命令定时器简单工厂
 * @author chenguoxiang
 * @create 2019-04-01 16:25
 **/
public class ITimeJobFactory {
    public enum TIMEJOB{
        CORE_SCHEDULE,INDEX,CLEAR,STOCKCODE,INFO,DAILY_BASIC,TOP_HOLDER,DY,YEAR_REPORT,DIVIDEND,DYROE,FUND_HOLDERS
    }
    private static HashMap<TIMEJOB,ITimerJob> map;

    static {
        map= new HashMap<>();
        map.put(TIMEJOB.CORE_SCHEDULE,new CoreScheduleTimerJobImpl());//核心调度任务负责管理所有任务调度---【代码启动立刻执行，周期1分钟】【父任务】
        map.put(TIMEJOB.INDEX,new IndexTimerJobImpl());//开盘价格涨幅抓取任务---  【股票开盘期间，周期1分钟】【开启一个12核心定时线程池，每个线程20s请求一次】【雪球】【新浪】【深交所】
        map.put(TIMEJOB.INFO,new InfoTimerJobImpl());//股票详情数据抓取任务---            【下午三点后执行，周期每天】【单线程执行】【同花顺】
        map.put(TIMEJOB.TOP_HOLDER,new TopHolderTimerJobImpl());//前10大股东抓取任务---   【下午三点后执行，周期每天】【单线程执行】【Tushare】【您每分钟最多访问该接口10次】
        map.put(TIMEJOB.DY,new XueQiuDyTimerJobImpl());//雪球dy更新任务---                【下午三点后执行，周期每天】
        map.put(TIMEJOB.FUND_HOLDERS,new FundHoldersTimerJobImpl());//基金持仓任务        【下午三点后执行，周期每天；并且必须在DY任务之后执行】
        map.put(TIMEJOB.CLEAR,new ClearTimerJobImpl());//0点要执行的清理工作---                  【凌晨0时01分执行，周期每天】
        map.put(TIMEJOB.YEAR_REPORT,new YearReportTimerJobImpl());//年报财务数据抓取任务--       【凌晨0时01分执行，周期每天】
        map.put(TIMEJOB.STOCKCODE,new StockCodeTimerJobImpl());//A股所有代码更新任务---          【凌晨0时01分执行，周期每天】
        map.put(TIMEJOB.DAILY_BASIC,new DailyBasicTimerJobImpl());//每日股票指标抓取任务---                    【凌晨1时0分，周期每天】
        map.put(TIMEJOB.DIVIDEND,new DividendTimerJobImpl());//ROE（净资产收益率）,DY（股息收益率）计算任务-   【凌晨1时0分，周期每天】
        map.put(TIMEJOB.DYROE,new DyRoeAnalysisJobImpl());//历史分红抓取任务                    【？？？没有找到调用代码】




    }

    /**
     * 得到指定的定时器
     * @param timejob
     * @return
     */
    public static ITimerJob getJob(TIMEJOB timejob){
        return map.get(timejob);
    }

    public static JSONArray getTasks(){
        JSONArray jsonArray = new JSONArray();
        JSONObject rows;
        for (TIMEJOB key :map.keySet()) {
            rows=new JSONObject();
            AbstractTimeJob job = (AbstractTimeJob) map.get(key);
            rows.put("id",key);
            rows.put("name",job.name);
            rows.put("status",job.status);
            jsonArray.add(rows);
        }
        return jsonArray;
    }

    /**
     * 得到任务状态
     * @param key
     * @return
     */
    public static ITimerJob.STATUS getJobStatus(TIMEJOB key){
        AbstractTimeJob job = (AbstractTimeJob) map.get(key);
        return job.status;
    }
}
