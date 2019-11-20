package com.lenovo.repository.data;

import com.lenovo.repository.data.service.ProcessorService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
@Log
public class StartListener implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private ProcessorService processorService;

    private final static  String START_URL =
            "http://servicekb.lenovo.com.cn/CN/KnowledgeBase/Lists/FAQs/DispForm.aspx?ID=26525";
            //"http://servicekb.lenovo.com.cn/CN/searchcenter/Pages/results.aspx?AllRange=1&start1=1";
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if(event.getApplicationContext().getParent() == null) {
            log.info("开始爬虫...");
            processorService.start(START_URL);
            log.info("爬虫结束...");
        }
    }
}
