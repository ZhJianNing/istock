package io.github.kingschan1204.istock.module.spider.schedule;

import io.github.kingschan1204.istock.common.util.spring.SpringContextUtil;
import io.github.kingschan1204.istock.module.spider.timerjob.ITimeJobFactory;
import io.github.kingschan1204.istock.module.spider.timerjob.ITimerJob;
import io.github.kingschan1204.istock.module.spider.util.TradingDateUtil;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * 调度作业时间
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
        boolean tradeday = tradingDateUtil.isTradingDay();
        if (!tradeday) {
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

            switch (dateTime.getHour()) {

                //下午三点更新【基本信息】【dy信息】【前十大股东】
                case 15:
                    //info信息
                    ITimeJobFactory.getJob(ITimeJobFactory.TIMEJOB.INFO).execute(ITimerJob.COMMAND.START);
                    //Dy
                    ITimeJobFactory.getJob(ITimeJobFactory.TIMEJOB.DY).execute(ITimerJob.COMMAND.START);
                    //top 10 holders
                    ITimeJobFactory.getJob(ITimeJobFactory.TIMEJOB.TOP_HOLDER).execute(ITimerJob.COMMAND.START);
                    break;

                //下午四点更新【基金持仓】
                case 16:
                    //基金持仓
                    log.info("dy任务status：{}", ITimeJobFactory.getJobStatus(ITimeJobFactory.TIMEJOB.DY));
                    ITimeJobFactory.getJob(ITimeJobFactory.TIMEJOB.FUND_HOLDERS).execute(ITimerJob.COMMAND.START);


                //晚上12点【清理任务】【更新所有代码list（已改为自选，可不执行）】【年报任务】
                //case 0:
                case 17:
                    //清理
                    //删除历史pb,pe,price,报表数据（应该会删除三个表stock_his_pe_pb、stock_report、stock_price_daily）,和data下载的文件
//                    ITimeJobFactory.getJob(ITimeJobFactory.TIMEJOB.CLEAR).execute(null);
                    // 更新所有代码，这个定时不用执行了，以为已改为自选代码了
//                    ITimeJobFactory.getJob(ITimeJobFactory.TIMEJOB.STOCKCODE).execute(null);
                    //年报
                    ITimeJobFactory.getJob(ITimeJobFactory.TIMEJOB.YEAR_REPORT).execute(ITimerJob.COMMAND.START);
                    break;


                //晚上1点更新【日常基本数据】【分红数据】
                case 1:
                    //TuShare每日指标数据,需要600积分，暂时不执行
//                ITimeJobFactory.getJob(ITimeJobFactory.TIMEJOB.DAILY_BASIC).execute(ITimerJob.COMMAND.START);
                    //分红数据
                    ITimeJobFactory.getJob(ITimeJobFactory.TIMEJOB.DIVIDEND).execute(ITimerJob.COMMAND.START);
                    break;







                default:
                    break;
            }
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
