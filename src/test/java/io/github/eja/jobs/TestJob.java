package io.github.eja.jobs;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import io.github.eja.annotation.ElasticJobConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * @author: ZhangYuSai
 * @version: 1.0.0
 */
@Slf4j
@ElasticJobConfig(cron = "0/5 * * * * ?")
public class TestJob implements SimpleJob {
  @Override
  public void execute(ShardingContext shardingContext) {
    log.debug("Job Running...");
  }
}
