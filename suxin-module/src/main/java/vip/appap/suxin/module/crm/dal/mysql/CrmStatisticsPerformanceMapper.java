package vip.appap.suxin.module.crm.dal.mysql;

import vip.appap.suxin.module.crm.controller.admin.vo.CrmStatisticsPerformanceReqVO;
import vip.appap.suxin.module.crm.controller.admin.vo.CrmStatisticsPerformanceRespVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * CRM 员工业绩分析 Mapper
 *
 * @author scholar
 */
@Mapper
public interface CrmStatisticsPerformanceMapper {

    /**
     * 员工签约合同数量
     *
     * @param performanceReqVO 参数
     * @return 员工签约合同数量
     */
    List<CrmStatisticsPerformanceRespVO> selectContractCountPerformance(CrmStatisticsPerformanceReqVO performanceReqVO);

    /**
     * 员工签约合同金额
     *
     * @param performanceReqVO 参数
     * @return 员工签约合同金额
     */
    List<CrmStatisticsPerformanceRespVO> selectContractPricePerformance(CrmStatisticsPerformanceReqVO performanceReqVO);

    /**
     * 员工回款金额
     *
     * @param performanceReqVO 参数
     * @return 员工回款金额
     */
    List<CrmStatisticsPerformanceRespVO> selectReceivablePricePerformance(CrmStatisticsPerformanceReqVO performanceReqVO);

}
