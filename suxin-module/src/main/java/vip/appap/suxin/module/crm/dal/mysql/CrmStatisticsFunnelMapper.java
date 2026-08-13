package vip.appap.suxin.module.crm.dal.mysql;

import vip.appap.suxin.module.crm.controller.admin.vo.CrmStatisticsBusinessInversionRateSummaryByDateRespVO;
import vip.appap.suxin.module.crm.controller.admin.vo.CrmStatisticsBusinessSummaryByDateRespVO;
import vip.appap.suxin.module.crm.controller.admin.vo.CrmStatisticsBusinessSummaryByEndStatusRespVO;
import vip.appap.suxin.module.crm.controller.admin.vo.CrmStatisticsFunnelReqVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * CRM 销售漏斗 Mapper
 *
 * @author HUIHUI
 */
@Mapper
public interface CrmStatisticsFunnelMapper {

    Long selectCustomerCountByDate(CrmStatisticsFunnelReqVO reqVO);

    Long selectBusinessCountByDateAndEndStatus(@Param("reqVO") CrmStatisticsFunnelReqVO reqVO, @Param("status") Integer status);

    List<CrmStatisticsBusinessSummaryByEndStatusRespVO> selectBusinessSummaryListGroupByEndStatus(CrmStatisticsFunnelReqVO reqVO);

    List<CrmStatisticsBusinessSummaryByDateRespVO> selectBusinessSummaryGroupByDate(CrmStatisticsFunnelReqVO reqVO);

    List<CrmStatisticsBusinessInversionRateSummaryByDateRespVO> selectBusinessInversionRateSummaryByDate(CrmStatisticsFunnelReqVO reqVO);

}
