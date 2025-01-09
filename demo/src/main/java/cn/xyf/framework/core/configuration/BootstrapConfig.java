package cn.xyf.framework.core.configuration;

import cn.xyf.framework.core.boot.Bootstrap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import java.util.Arrays;

@Configuration
@ComponentScan(basePackages = {"cn.xyf.framework"})
public class BootstrapConfig {

    @Bean(initMethod = "init")
    public Bootstrap tdFrameworkBootstrap() {
        Bootstrap bootstrap = new Bootstrap();

        // 设置 packages 属性
        bootstrap.setPackages(Arrays.asList("cn.xyf"));
        
        return bootstrap;
    }
}
