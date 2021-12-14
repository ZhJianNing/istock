package io.github.kingschan1204.istock.module.spider.schedule;

import io.github.kingschan1204.istock.common.util.spring.SpringContextUtil;
import io.github.kingschan1204.istock.module.spider.timerjob.ITimeJobFactory;
import io.github.kingschan1204.istock.module.spider.timerjob.ITimerJob;
import io.github.kingschan1204.istock.module.spider.util.TradingDateUtil;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;

/**
 * 调度作业
 *
 * @author chenguoxiang
 * @create 2019-03-26 17:00
 **/
@Slf4j
public class ScheduleThread implements Runnable {


    //没分钟执行一次
    void jobProcess() throws Exception {

        //股票时间工具
        TradingDateUtil tradingDateUtil = SpringContextUtil.getBean(TradingDateUtil.class);
        boolean tradeday=tradingDateUtil.isTradingDay();
        if(!tradeday){
            log.info("不是交易日，不工作！！！");
            return;
        }
        LocalDateTime dateTime = LocalDateTime.now();
        //如果是交易时间，开盘价格涨幅抓取任务
        if (tradingDateUtil.isTradingTimeNow()) {
            ITimeJobFactory.getJob(ITimeJobFactory.TIMEJOB.INDEX).execute(ITimerJob.COMMAND.START);
        } else {
            //不是交易时间，关闭开盘期间股票价格抓取任务
            ITimeJobFactory.getJob(ITimeJobFactory.TIMEJOB.INDEX).execute(ITimerJob.COMMAND.STOP);
            if(dateTime.getHour()>=15){
                //下午3点  闭市后爬取info信息
//                ITimeJobFactory.getJob(ITimeJobFactory.TIMEJOB.INFO).execute(ITimerJob.COMMAND.START);
                //Dy
                ITimeJobFactory.getJob(ITimeJobFactory.TIMEJOB.DY).execute(ITimerJob.COMMAND.START);
//                //top 10 holders
//                ITimeJobFactory.getJob(ITimeJobFactory.TIMEJOB.TOP_HOLDER).execute(ITimerJob.COMMAND.START);
//                //基金持股
//                if(ITimeJobFactory.getJobStatus(ITimeJobFactory.TIMEJOB.DY)== ITimerJob.STATUS.STOP){
//                    ITimeJobFactory.getJob(ITimeJobFactory.TIMEJOB.FUND_HOLDERS).execute(ITimerJob.COMMAND.START);
//                }
            }
        }


        switch (dateTime.getHour()) {
            case 0:
                //晚上12点
                if(dateTime.getMinute()==1){
                    //清理
                    ITimeJobFactory.getJob(ITimeJobFactory.TIMEJOB.CLEAR).execute(null);
                    // code company
                    ITimeJobFactory.getJob(ITimeJobFactory.TIMEJOB.STOCKCODE).execute(null);
                    //year report
                    ITimeJobFactory.getJob(ITimeJobFactory.TIMEJOB.YEAR_REPORT).execute(ITimerJob.COMMAND.START);
                }
                break;
            case 1:
                ITimeJobFactory.getJob(ITimeJobFactory.TIMEJOB.DAILY_BASIC).execute(ITimerJob.COMMAND.START);
                ITimeJobFactory.getJob(ITimeJobFactory.TIMEJOB.DIVIDEND).execute(ITimerJob.COMMAND.START);
                break;
            case 9:
                //早上9点
                break;
            case 11:
                //上午11点
                break;
            case 13:
                //下午1点
                break;
            case 15:
                break;
            default:
                break;
        }
    }

    @Override
    public void run() {
        try {
            jobProcess();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
