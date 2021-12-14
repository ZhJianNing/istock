package io.github.kingschan1204.istock.module.spider.timerjob.impl;

import io.github.kingschan1204.istock.module.spider.SimpleTimerJobContainer;
import io.github.kingschan1204.istock.module.spider.crawl.info.XueQiuQuoteSpider;
import io.github.kingschan1204.istock.module.spider.timerjob.AbstractTimeJob;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class XueQiuDyTimerJobImpl extends AbstractTimeJob {

    private SimpleTimerJobContainer dyCrawlJob;
    private AtomicInteger error=new AtomicInteger(0);

    public XueQiuDyTimerJobImpl(){
        name="雪球dy更新任务";
    }

    @Override
    public void execute(COMMAND command) throws Exception {
        switch (command){
            case START:
                if (null == dyCrawlJob) {
                    if(error.get()>10){
                        log.error("雪球token失效,错误超过10次,不执行Dy任务！");
                        status=STATUS.ERROR;
                        return;
                    }
                    log.info("开启dy更新线程!");
                    XueQiuQuoteSpider xueQiuQuoteSpider = new XueQiuQuoteSpider(error);
                    dyCrawlJob = new SimpleTimerJobContainer(
                            //雪球对访问频率有限制，100/s（频繁），20/s(频繁）,10/s(频繁）,5/s(频繁），2/s(目前没有被限制）
                            //过于频繁后，ip被限制，需要在页面进行验证，重新解锁
                            xueQiuQuoteSpider,0,500, TimeUnit.MILLISECONDS,"雪球dy更新任务",4);
                    new Thread(dyCrawlJob, "雪球dy更新任务").start();
                    status=STATUS.RUN;
                }
                break;
            case STOP:
                if (null != dyCrawlJob) {
                    log.info("关闭Dy更新线程!");
                    dyCrawlJob.shutDown();
                    dyCrawlJob = null;
                    status=STATUS.STOP;
                }
                break;
            default:
                log.error("发现未知命令:{} " , command);
        }
    }
}
