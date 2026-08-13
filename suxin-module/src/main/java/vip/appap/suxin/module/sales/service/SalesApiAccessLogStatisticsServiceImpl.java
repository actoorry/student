package vip.appap.suxin.module.sales.service;

import vip.appap.suxin.module.sales.dal.mysql.SalesApiAccessLogStatisticsMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;

/**
 * API 访问日志的统计 Service 实现类
 *
 * @author owen
 */
@Service
@Validated
public class SalesApiAccessLogStatisticsServiceImpl implements SalesApiAccessLogStatisticsService {

    @Resource
    private SalesApiAccessLogStatisticsMapper apiAccessLogStatisticsMapper;

    @Override
    public Integer getUserCount(Integer userType, LocalDateTime beginTime, LocalDateTime endTime) {
        return apiAccessLogStatisticsMapper.selectUserCountByUserTypeAndCreateTimeBetween(userType, beginTime, endTime);
    }

    @Override
    public Integer getIpCount(Integer userType, LocalDateTime beginTime, LocalDateTime endTime) {
        return apiAccessLogStatisticsMapper.selectIpCountByUserTypeAndCreateTimeBetween(userType, beginTime, endTime);
    }

}
