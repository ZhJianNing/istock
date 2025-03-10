package io.github.kingschan1204.istock.module.spider.timerjob.impl;

import io.github.kingschan1204.istock.module.spider.SimpleTimerJobContainer;
import io.github.kingschan1204.istock.module.spider.crawl.info.ThsInfoSpider;
import io.github.kingschan1204.istock.module.spider.timerjob.AbstractTimeJob;
import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.TimeUnit;

/**
 * 代码info信息更新命令封装
 * @author chenguoxiang
 * @create 2019-04-01 16:21
 **/
@Slf4j
public class InfoTimerJobImpl extends AbstractTimeJob {

    private SimpleTimerJobContainer infoCrawlJob;

    public InfoTimerJobImpl(){
        name="股票详情数据抓取任务";
    }

    @Override
    public void execute(COMMAND command) throws Exception {
        switch (command){
            case START:
                if (null == infoCrawlJob) {
                    log.info("开启info更新线程!");
                    ThsInfoSpider infoSpider = new ThsInfoSpider();
                    infoCrawlJob = new SimpleTimerJobContainer(infoSpider,0,20, TimeUnit.MILLISECONDS,"股票详情",10);
                    new Thread(infoCrawlJob, "股票详情").start();
                    status=STATUS.RUN;
                }
                break;
            case STOP:
                if (null != infoCrawlJob) {
                    log.info("关闭股票详情更新线程!");
                    infoCrawlJob.shutDown();
                    infoCrawlJob = null;
                    status=STATUS.STOP;
                }
                break;
            default:
                log.error("发现未知命令:{} " , command);
        }
    }
}
