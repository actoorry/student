package vip.appap.suxin.framework.quartz.config;

import vip.appap.suxin.framework.quartz.core.jdbc.QuartzLowercaseTableDataSource;
import vip.appap.suxin.framework.quartz.core.scheduler.SchedulerManager;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.quartz.QuartzDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.sql.DataSource;
import java.util.Optional;

/**
 * 定时任务 Configuration
 */
@AutoConfiguration
@EnableScheduling // 开启 Spring 自带的定时任务
@Slf4j
public class SuxinQuartzAutoConfiguration {

    @Bean
    @QuartzDataSource
    @ConditionalOnProperty(prefix = "suxin.quartz", name = "lowercase-table-names", havingValue = "true")
    public DataSource quartzLowercaseTableDataSource(@Qualifier("dataSource") DataSource dataSource) {
        return new QuartzLowercaseTableDataSource(dataSource);
    }

    @Bean
    public SchedulerManager schedulerManager(Optional<Scheduler> scheduler) {
        if (!scheduler.isPresent()) {
            log.info("[定时任务 - 已禁用][参考 https://doc.appap.vip/job/ 开启]");
            return new SchedulerManager(null);
        }
        return new SchedulerManager(scheduler.get());
    }

}
