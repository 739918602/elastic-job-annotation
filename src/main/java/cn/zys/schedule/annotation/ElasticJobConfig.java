package cn.zys.schedule.annotation;

import com.dangdang.ddframe.job.lite.api.listener.AbstractDistributeOnceElasticJobListener;
import com.dangdang.ddframe.job.lite.api.listener.ElasticJobListener;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @Title:
 * @Author: ZhangYuSai
 * @Date: 2018/8/15 15:59
 * @Version: 1.0.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface ElasticJobConfig {
    String cron();

    int shardingTotalCount() default 1;

    String shardingItemParameters() default "";

    String jobParameter() default "";

    boolean failover() default false;

    boolean misfire() default true;

    String description() default "";

    String jobExceptionHandler() default "";

    String executorServiceHandler() default "";

    boolean streamingProcess() default false;

    String scriptCommandLine() default "";

    boolean monitorExecution() default true;

    int monitorPort() default -1;

    int maxTimeDiffSeconds() default -1;

    String jobShardingStrategyClass() default "";

    int reconcileIntervalMinutes() default 10;

    String eventTraceRdbDataSource() default "dataSource";

    boolean eventTraceIsOpen() default false;

    boolean overwrite() default true;

    boolean disabled() default false;

    Class<? extends ElasticJobListener> listener() default ElasticJobListener.class;

    Class<? extends AbstractDistributeOnceElasticJobListener> distributedListener() default AbstractDistributeOnceElasticJobListener.class;

    long startedTimeoutMilliseconds() default 9223372036854775807L;

    long completedTimeoutMilliseconds() default 9223372036854775807L;
}
