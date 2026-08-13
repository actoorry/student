package vip.appap.suxin.module.hr.service.outbound;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.hr.controller.admin.outbound.vo.OutboundPageReqVO;
import vip.appap.suxin.module.hr.controller.admin.outbound.vo.OutboundRespVO;
import vip.appap.suxin.module.hr.controller.admin.outbound.vo.OutboundSaveReqVO;
import vip.appap.suxin.module.hr.dal.dataobject.outbound.OutboundDO;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.List;

/**
 * HR 人员外出管理 Service 接口
 *
 * 不走审批流程，由人事自行填写维护。
 *
 * @author suxin
 */
public interface OutboundService {

    /**
     * 创建外出记录
     *
     * @param createReqVO 创建信息
     * @return 外出记录编号
     */
    Long createOutbound(@Valid OutboundSaveReqVO createReqVO);

    /**
     * 更新外出记录
     *
     * @param updateReqVO 更新信息
     */
    void updateOutbound(@Valid OutboundSaveReqVO updateReqVO);

    /**
     * 获得外出记录（联表含员工姓名/工号/部门）
     *
     * @param id 编号
     * @return 外出记录
     */
    OutboundRespVO getOutbound(Long id);

    /**
     * 获得外出记录分页
     *
     * @param pageReqVO 分页查询
     * @return 分页结果（含员工姓名等联表字段）
     */
    PageResult<OutboundRespVO> getOutboundPage(OutboundPageReqVO pageReqVO);

    /**
     * 删除外出记录
     *
     * @param id 编号
     */
    void deleteOutbound(Long id);

    /**
     * 查询员工计入汇总的外出记录（用于员工档案汇总 Tab）
     *
     * @param partnerId 员工 partner.id
     * @return 有效记录列表
     */
    List<OutboundDO> getEffectiveOutboundListByPartnerId(Long partnerId);

    /**
     * 累计下乡支援服务年限（副高职称评审依据）
     *
     * @param partnerId 员工 partner.id
     * @return 累计年限（保留 1 位小数）
     */
    BigDecimal sumSupportYears(Long partnerId);

    /**
     * 累计继教学分（进修/学习/培训）
     *
     * @param partnerId 员工 partner.id
     * @return 累计学分
     */
    BigDecimal sumContinuingEducationCredit(Long partnerId);

}
