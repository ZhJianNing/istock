package io.github.kingschan1204.istock.module.spider.timerjob.impl;

import io.github.kingschan1204.istock.module.spider.SimpleTimerJobContainer;
import io.github.kingschan1204.istock.module.spider.crawl.report.YearReportSpider;
import io.github.kingschan1204.istock.module.spider.timerjob.AbstractTimeJob;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class YearReportTimerJobImpl extends AbstractTimeJob {

    private SimpleTimerJobContainer yearReportJob;
    private AtomicInteger error=new AtomicInteger(0);

    public YearReportTimerJobImpl(){
        name="年报财务数据抓取任务";
    }

    @Override
    public void execute(COMMAND command) throws Exception {
        switch (command){
            case START:
                if (null == yearReportJob) {
                    if(error.get()>30){
                        log.error("年报任务 错误超过30次,停止执行！");
                        status=STATUS.ERROR;
                        return;
                    }
                    log.info("开启年报任务线程!");
                    YearReportSpider yearReportSpider = new YearReportSpider(error);
                    yearReportJob = new SimpleTimerJobContainer(yearReportSpider,0,5, TimeUnit.SECONDS,"年报任务",4);
                    new Thread(yearReportJob, "年报任务").start();
                    status=STATUS.RUN;
                }
                break;
            case STOP:
                if (null != yearReportJob) {
                    log.info("关闭年报任务!");
                    yearReportJob.shutDown();
                    yearReportJob = null;
                    status=STATUS.STOP;
                }
                break;
            default:
                log.error("发现未知命令:{} " , command);
        }
    }
}
