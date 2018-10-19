package cn.zys.schedule.autoconfigure;

import com.dangdang.ddframe.job.event.JobEventListener;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbIdentity;
import com.dangdang.ddframe.job.event.type.JobExecutionEvent;
import com.dangdang.ddframe.job.event.type.JobStatusTraceEvent;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * @Title:
 * @Author: ZhangYuSai
 * @Date: 2018/10/8 11:18
 * @Version: 1.0.0
 */
public class JobEventNoSqlListener extends JobEventRdbIdentity implements JobEventListener {
    private final transient MongoTemplate mongoTemplate;
    public JobEventNoSqlListener(MongoDbFactory mongoDbFactory) {
        mongoTemplate = new MongoTemplate(mongoDbFactory);
    }

    @Override
    public void listen(JobExecutionEvent jobExecutionEvent) {
        mongoTemplate.save(jobExecutionEvent,"JobExecutionEvent");
    }

    @Override
    public void listen(JobStatusTraceEvent jobStatusTraceEvent) {
        mongoTemplate.save(jobStatusTraceEvent,"JobStatusTraceEvent");
    }
}
