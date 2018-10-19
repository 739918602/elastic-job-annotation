package cn.zys.schedule.autoconfigure;


import cn.zys.schedule.annotation.ElasticJobConfig;
import com.dangdang.ddframe.job.api.ElasticJob;
import com.dangdang.ddframe.job.api.JobType;
import com.dangdang.ddframe.job.api.dataflow.DataflowJob;
import com.dangdang.ddframe.job.api.script.ScriptJob;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.JobCoreConfiguration.Builder;
import com.dangdang.ddframe.job.config.JobTypeConfiguration;
import com.dangdang.ddframe.job.config.dataflow.DataflowJobConfiguration;
import com.dangdang.ddframe.job.config.script.ScriptJobConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.event.JobEventConfiguration;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbConfiguration;
import com.dangdang.ddframe.job.executor.handler.JobProperties.JobPropertiesEnum;
import com.dangdang.ddframe.job.lite.api.listener.AbstractDistributeOnceElasticJobListener;
import com.dangdang.ddframe.job.lite.api.listener.ElasticJobListener;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.spring.api.SpringJobScheduler;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.*;
/**
 * @Title:
 * @Author: ZhangYuSai
 * @Date: 2018/8/15 16:15
 * @Version: 1.0.0
 */
@Configuration
@ConditionalOnProperty(name = "elasticJob.zkAddressList")
@EnableConfigurationProperties({ZookeeperRegistryProperties.class})
public class ElasticJobAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(ElasticJobAutoConfiguration.class);
    @Resource
    private ZookeeperRegistryCenter regCenter;
    @Resource
    private ApplicationContext applicationContext;
    @Autowired
    private ZookeeperRegistryProperties zookeeperRegistryProperties;

    public ElasticJobAutoConfiguration() {
    }

    @PostConstruct
    public void init() {
        Map<String, ElasticJob> elasticJobMap = this.applicationContext.getBeansOfType(ElasticJob.class);
        Iterator var2 = elasticJobMap.values().iterator();

        while(var2.hasNext()) {
            ElasticJob elasticJob = (ElasticJob)var2.next();
            Class<? extends ElasticJob> jobClass = elasticJob.getClass();
            ElasticJobConfig elasticJobConfig = (ElasticJobConfig)jobClass.getAnnotation(ElasticJobConfig.class);
            LiteJobConfiguration liteJobConfiguration = this.getLiteJobConfiguration(this.getJobType(elasticJob), jobClass, elasticJobConfig);
            JobEventConfiguration jobEventConfiguration = null;
            if (elasticJobConfig.eventTraceIsOpen()) {
                jobEventConfiguration = this.getJobEventConfiguration(elasticJobConfig.eventTraceRdbDataSource());
            }
            ElasticJobListener[] elasticJobListeners = this.createElasticJobListeners(elasticJobConfig);
            elasticJobListeners = Objects.isNull(elasticJobListeners) ? new ElasticJobListener[0] : elasticJobListeners;
            if (Objects.isNull(jobEventConfiguration)) {
                (new SpringJobScheduler(elasticJob, this.regCenter, liteJobConfiguration, elasticJobListeners)).init();
            } else {
                (new SpringJobScheduler(elasticJob, this.regCenter, liteJobConfiguration, jobEventConfiguration, elasticJobListeners)).init();
            }
        }

    }



    private JobEventConfiguration getJobEventConfiguration(String eventTraceRdbDataSource) {
        if (StringUtils.isBlank(eventTraceRdbDataSource)) {
            return null;
        } else if (!this.applicationContext.containsBean(eventTraceRdbDataSource)) {
            throw new RuntimeException("not exist datasource [" + eventTraceRdbDataSource + "] !");
        } else {
            Object dataSource = this.applicationContext.getBean(eventTraceRdbDataSource);
            if(dataSource instanceof DataSource){
                return new JobEventRdbConfiguration((DataSource) dataSource);
            }else if(dataSource instanceof MongoDbFactory){
                return new JobEventNoSqlConfiguration((MongoDbFactory)dataSource);
            }else{
                throw new RuntimeException("wrong data source type  [" + eventTraceRdbDataSource + "] !");
            }
        }
    }

    private JobType getJobType(ElasticJob elasticJob) {
        if (elasticJob instanceof SimpleJob) {
            return JobType.SIMPLE;
        } else if (elasticJob instanceof DataflowJob) {
            return JobType.DATAFLOW;
        } else if (elasticJob instanceof ScriptJob) {
            return JobType.SIMPLE;
        } else {
            throw new RuntimeException("unknown JobType [" + elasticJob.getClass() + "]!");
        }
    }

    private JobCoreConfiguration getJobCoreConfiguration(String jobName, ElasticJobConfig elasticJobConfig) {
        Builder builder = JobCoreConfiguration.newBuilder(jobName, elasticJobConfig.cron(), elasticJobConfig.shardingTotalCount()).shardingItemParameters(elasticJobConfig.shardingItemParameters()).jobParameter(elasticJobConfig.jobParameter()).failover(elasticJobConfig.failover()).misfire(elasticJobConfig.misfire()).description(elasticJobConfig.description());
        if (StringUtils.isNotBlank(elasticJobConfig.jobExceptionHandler())) {
            builder.jobProperties(JobPropertiesEnum.JOB_EXCEPTION_HANDLER.getKey(), elasticJobConfig.jobExceptionHandler());
        }

        if (StringUtils.isNotBlank(elasticJobConfig.executorServiceHandler())) {
            builder.jobProperties(JobPropertiesEnum.EXECUTOR_SERVICE_HANDLER.getKey(), elasticJobConfig.executorServiceHandler());
        }

        return builder.build();
    }

    private LiteJobConfiguration getLiteJobConfiguration(JobType jobType, Class<? extends ElasticJob> jobClass, ElasticJobConfig elasticJobConfig) {
        JobCoreConfiguration jobCoreConfiguration = this.getJobCoreConfiguration(jobClass.getName(), elasticJobConfig);
        JobTypeConfiguration jobTypeConfiguration = this.getJobTypeConfiguration(jobCoreConfiguration, jobType, jobClass.getCanonicalName(), elasticJobConfig.streamingProcess(), elasticJobConfig.scriptCommandLine());
        return LiteJobConfiguration.newBuilder((JobTypeConfiguration)Objects.requireNonNull(jobTypeConfiguration)).monitorExecution(elasticJobConfig.monitorExecution()).monitorPort(elasticJobConfig.monitorPort()).maxTimeDiffSeconds(elasticJobConfig.maxTimeDiffSeconds()).jobShardingStrategyClass(elasticJobConfig.jobShardingStrategyClass()).reconcileIntervalMinutes(elasticJobConfig.reconcileIntervalMinutes()).disabled(elasticJobConfig.disabled()).overwrite(elasticJobConfig.overwrite()).build();
    }

    private JobTypeConfiguration getJobTypeConfiguration(JobCoreConfiguration jobCoreConfiguration, JobType jobType, String jobClass, boolean streamingProcess, String scriptCommandLine) {
        switch(jobType) {
            case DATAFLOW:
                return new DataflowJobConfiguration(jobCoreConfiguration, jobClass, streamingProcess);
            case SCRIPT:
                return new ScriptJobConfiguration(jobCoreConfiguration, scriptCommandLine);
            case SIMPLE:
            default:
                return new SimpleJobConfiguration(jobCoreConfiguration, jobClass);
        }
    }

    private ElasticJobListener[] createElasticJobListeners(ElasticJobConfig elasticJobConfig) {
        List<ElasticJobListener> elasticJobListeners = new ArrayList(2);
        ElasticJobListener elasticJobListener = this.createElasticJobListener(elasticJobConfig.listener());
        if (Objects.nonNull(elasticJobListener)) {
            elasticJobListeners.add(elasticJobListener);
        }

        AbstractDistributeOnceElasticJobListener distributedListener = this.createAbstractDistributeOnceElasticJobListener(elasticJobConfig.distributedListener(), elasticJobConfig.startedTimeoutMilliseconds(), elasticJobConfig.completedTimeoutMilliseconds());
        if (Objects.nonNull(distributedListener)) {
            elasticJobListeners.add(distributedListener);
        }

        if (CollectionUtils.isEmpty(elasticJobListeners)) {
            return null;
        } else {
            ElasticJobListener[] elasticJobListenerArray = new ElasticJobListener[elasticJobListeners.size()];

            for(int i = 0; i < elasticJobListeners.size(); ++i) {
                elasticJobListenerArray[i] = (ElasticJobListener)elasticJobListeners.get(i);
            }

            return elasticJobListenerArray;
        }
    }

    private ElasticJobListener createElasticJobListener(Class<? extends ElasticJobListener> listener) {
        if (listener.isInterface()) {
            return null;
        } else {
            return this.applicationContext.containsBean(listener.getSimpleName()) ? (ElasticJobListener)this.applicationContext.getBean(listener.getSimpleName(), ElasticJobListener.class) : this.registerElasticJobListener(listener);
        }
    }

    private AbstractDistributeOnceElasticJobListener createAbstractDistributeOnceElasticJobListener(Class<? extends AbstractDistributeOnceElasticJobListener> distributedListener, long startedTimeoutMilliseconds, long completedTimeoutMilliseconds) {
        if (Objects.equals(distributedListener, AbstractDistributeOnceElasticJobListener.class)) {
            return null;
        } else {
            return this.applicationContext.containsBean(distributedListener.getSimpleName()) ? (AbstractDistributeOnceElasticJobListener)this.applicationContext.getBean(distributedListener.getSimpleName(), AbstractDistributeOnceElasticJobListener.class) : this.registerAbstractDistributeOnceElasticJobListener(distributedListener, startedTimeoutMilliseconds, completedTimeoutMilliseconds);
        }
    }

    private ElasticJobListener registerElasticJobListener(Class<? extends ElasticJobListener> listener) {
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.rootBeanDefinition(listener);
        beanDefinitionBuilder.setScope("prototype");
        this.getDefaultListableBeanFactory().registerBeanDefinition(listener.getSimpleName(), beanDefinitionBuilder.getBeanDefinition());
        return (ElasticJobListener)this.applicationContext.getBean(listener.getSimpleName(), listener);
    }

    private AbstractDistributeOnceElasticJobListener registerAbstractDistributeOnceElasticJobListener(Class<? extends AbstractDistributeOnceElasticJobListener> distributedListener, long startedTimeoutMilliseconds, long completedTimeoutMilliseconds) {
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.rootBeanDefinition(distributedListener);
        beanDefinitionBuilder.setScope("prototype");
        beanDefinitionBuilder.addConstructorArgValue(startedTimeoutMilliseconds);
        beanDefinitionBuilder.addConstructorArgValue(completedTimeoutMilliseconds);
        this.getDefaultListableBeanFactory().registerBeanDefinition(distributedListener.getSimpleName(), beanDefinitionBuilder.getBeanDefinition());
        return (AbstractDistributeOnceElasticJobListener)this.applicationContext.getBean(distributedListener.getSimpleName(), distributedListener);
    }

    private DefaultListableBeanFactory getDefaultListableBeanFactory() {
        return (DefaultListableBeanFactory)((ConfigurableApplicationContext)this.applicationContext).getBeanFactory();
    }
}
