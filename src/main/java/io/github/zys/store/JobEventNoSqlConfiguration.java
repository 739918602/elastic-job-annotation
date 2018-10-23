package io.github.zys.store;

import com.dangdang.ddframe.job.event.JobEventConfiguration;
import com.dangdang.ddframe.job.event.JobEventListener;
import com.dangdang.ddframe.job.event.JobEventListenerConfigurationException;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbIdentity;
import com.mongodb.MongoException;
import org.springframework.data.mongodb.MongoDbFactory;

import java.io.Serializable;

/**
 * @author: ZhangYuSai
 * @version: 1.0.0
 */
public class JobEventNoSqlConfiguration extends JobEventRdbIdentity implements JobEventConfiguration, Serializable {
    private final transient  MongoDbFactory mongoDbFactory;
    @Override
    public JobEventListener createJobEventListener() throws JobEventListenerConfigurationException {
        try{
            return new JobEventNoSqlListener(mongoDbFactory);
        }catch (MongoException ex){
            throw new JobEventListenerConfigurationException(ex);
        }
    }

    public JobEventNoSqlConfiguration(MongoDbFactory mongoDbFactory) {
        this.mongoDbFactory = mongoDbFactory;
    }
}
