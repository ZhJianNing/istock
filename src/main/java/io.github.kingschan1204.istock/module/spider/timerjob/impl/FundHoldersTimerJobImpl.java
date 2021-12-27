package io.github.kingschan1204.istock.module.spider.timerjob.impl;

import io.github.kingschan1204.istock.module.spider.SimpleTimerJobContainer;
import io.github.kingschan1204.istock.module.spider.crawl.topholders.FundHolderSpider;
import io.github.kingschan1204.istock.module.spider.timerjob.AbstractTimeJob;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 基金持仓任务
 */
@Slf4j
public class FundHoldersTimerJobImpl extends AbstractTimeJob {

    private SimpleTimerJobContainer foundHeldCrawlJob;
    private AtomicInteger error = new AtomicInteger(0);

    public FundHoldersTimerJobImpl() {
        name = "基金持仓任务";
    }

    @Override
    public void execute(COMMAND command) throws Exception {
        switch (command) {
            case START:
                if (null == foundHeldCrawlJob) {
                    if (error.get() > 10) {
                        log.error("雪球token失效,错误超过10次,不执行FundHeld任务！");
                        status = STATUS.ERROR;
                        return;
                    }
                    log.info("开启基金持仓任务更新线程!");
                    FundHolderSpider infoSpider = new FundHolderSpider(error);
                    foundHeldCrawlJob = new SimpleTimerJobContainer(infoSpider, 0, 2, TimeUnit.SECONDS, "基金持仓任务", 4);
                    new Thread(foundHeldCrawlJob, "基金持仓任务").start();
                    status = STATUS.RUN;
                }
                break;
            case STOP:
                if (null != foundHeldCrawlJob) {
                    log.info("关闭基金持仓任务更新线程!");
                    foundHeldCrawlJob.shutDown();
                    foundHeldCrawlJob = null;
                    status = STATUS.STOP;
                }
                break;
            default:
                log.error("发现未知命令:{} ", command);
        }
    }
}
