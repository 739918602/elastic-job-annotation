package cn.zys.schedule.autoconfigure;



import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @Title:
 * @Author: ZhangYuSai
 * @Date: 2018/8/15 16:04
 * @Version: 1.0.0
 */
@ConfigurationProperties(
        prefix = "elasticJob"
)
@Data
public class ZookeeperRegistryProperties {
    private String zkAddressList;
    private String namespace;
    private int baseSleepTimeMilliseconds = 1000;
    private int maxSleepTimeMilliseconds = 3000;
    private int maxRetries = 3;
    private int connectionTimeoutMilliseconds = 15000;
    private int sessionTimeoutMilliseconds = 60000;
    private String digest;
}
