package io.github.kingschan1204.istock.module.spider.timerjob.impl;

import io.github.kingschan1204.istock.module.spider.SimpleTimerJobContainer;
import io.github.kingschan1204.istock.module.spider.crawl.topholders.TopHoldersSpider;
import io.github.kingschan1204.istock.module.spider.timerjob.AbstractTimeJob;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * 股东更新命令封装
 * @author chenguoxiang
 * @create 2019-04-01 16:21
 **/
@Slf4j
public class TopHolderTimerJobImpl extends AbstractTimeJob {

    private SimpleTimerJobContainer topHoldersCrawlJob;

    public TopHolderTimerJobImpl(){
        name="前10大股东抓取任务";
    }

    @Override
    public void execute(COMMAND command) throws Exception {
        switch (command){
            case START:
                if(null==topHoldersCrawlJob){
                    log.info("开始前十大股东抓取任务...");
                    TopHoldersSpider topHoldersSpider = new TopHoldersSpider();
                    topHoldersCrawlJob=new SimpleTimerJobContainer(topHoldersSpider,0,4, TimeUnit.SECONDS,"前十大股东",4);
                    Thread thread = new Thread(topHoldersCrawlJob);
                    thread.start();
                    status=STATUS.RUN;
                }
                break;
            case STOP:
                if(null!=topHoldersCrawlJob){
                    log.info("关闭前十大股东抓取任务 !");
                    topHoldersCrawlJob.shutDown();
                    topHoldersCrawlJob=null;
                    status=STATUS.STOP;
                }
                break;
            default:
                log.error("发现未知命令:{} " , command);
        }
    }
}
