package cn.zys.schedule.autoconfigure;

/**
 * @Title:
 * @Author: ZhangYuSai
 * @Date: 2018/8/15 16:03
 * @Version: 1.0.0
 */

import cn.zys.schedule.annotation.ElasticJobConfig;
import com.dangdang.ddframe.job.api.ElasticJob;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass({ElasticJob.class})
@ConditionalOnBean(
        annotation = {ElasticJobConfig.class}
)
@EnableConfigurationProperties({ZookeeperRegistryProperties.class})
public class RegistryCenterConfiguration {
    private final ZookeeperRegistryProperties regCenterProperties;

    @Autowired
    public RegistryCenterConfiguration(ZookeeperRegistryProperties regCenterProperties) {
        this.regCenterProperties = regCenterProperties;
    }

    @Bean(
            initMethod = "init"
    )
    @ConditionalOnMissingBean
    public ZookeeperRegistryCenter regCenter() {
        ZookeeperConfiguration zookeeperConfiguration = new ZookeeperConfiguration(this.regCenterProperties.getZkAddressList(), this.regCenterProperties.getNamespace());
        zookeeperConfiguration.setBaseSleepTimeMilliseconds(this.regCenterProperties.getBaseSleepTimeMilliseconds());
        zookeeperConfiguration.setConnectionTimeoutMilliseconds(this.regCenterProperties.getConnectionTimeoutMilliseconds());
        zookeeperConfiguration.setMaxSleepTimeMilliseconds(this.regCenterProperties.getMaxSleepTimeMilliseconds());
        zookeeperConfiguration.setSessionTimeoutMilliseconds(this.regCenterProperties.getSessionTimeoutMilliseconds());
        zookeeperConfiguration.setMaxRetries(this.regCenterProperties.getMaxRetries());
        zookeeperConfiguration.setDigest(this.regCenterProperties.getDigest());
        return new ZookeeperRegistryCenter(zookeeperConfiguration);
    }
}
