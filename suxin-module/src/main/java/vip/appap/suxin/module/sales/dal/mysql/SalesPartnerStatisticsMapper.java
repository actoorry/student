package vip.appap.suxin.module.sales.dal.mysql;

import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesPartnerStatisticsRegisterCountRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesPartnerStatisticsSexStatisticsRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesPartnerStatisticsTerminalStatisticsRespVO;
import vip.appap.suxin.module.sales.service.bo.SalesPartnerStatisticsAreaStatisticsRespBO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 会员信息的统计 Mapper
 *
 * @author owen
 */
@Mapper
@SuppressWarnings("rawtypes")
public interface SalesPartnerStatisticsMapper extends BaseMapperX {

    List<SalesPartnerStatisticsAreaStatisticsRespBO> selectSummaryListByAreaId();

    List<SalesPartnerStatisticsSexStatisticsRespVO> selectSummaryListBySex();

    List<SalesPartnerStatisticsTerminalStatisticsRespVO> selectSummaryListByRegisterTerminal();

    Integer selectUserCount(@Param("beginTime") LocalDateTime beginTime,
                            @Param("endTime") LocalDateTime endTime);

    /**
     * 获得用户的每天注册数量列表
     *
     * @param beginTime 开始时间
     * @param endTime 结束时间
     * @return 每天注册数量列表
     */
    List<SalesPartnerStatisticsRegisterCountRespVO> selectListByCreateTimeBetween(@Param("beginTime") LocalDateTime beginTime,
                                                                  @Param("endTime") LocalDateTime endTime);

}
