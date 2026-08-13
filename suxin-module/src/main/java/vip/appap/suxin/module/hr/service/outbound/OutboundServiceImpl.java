package vip.appap.suxin.module.hr.service.outbound;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.hr.controller.admin.outbound.vo.OutboundPageReqVO;
import vip.appap.suxin.module.hr.controller.admin.outbound.vo.OutboundRespVO;
import vip.appap.suxin.module.hr.controller.admin.outbound.vo.OutboundSaveReqVO;
import vip.appap.suxin.module.hr.dal.dataobject.outbound.OutboundDO;
import vip.appap.suxin.module.hr.dal.mysql.outbound.OutboundMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.module.hr.enums.ErrorCodeConstants.OUTBOUND_NOT_EXISTS;

/**
 * HR 人员外出管理 Service 实现类
 *
 * 业务规则（保存时即计算，不走审批）：
 * 1. 下乡支援（rural_support）：supportYears=天数/365（1位小数），计入副高评审服务年限
 * 2. 进修/学习/培训（training/study/course）：继教学分计入累计
 * 3. 保存时 effective=1，计入汇总统计
 *
 * @author suxin
 */
@Service
@Validated
public class OutboundServiceImpl implements OutboundService {

    /**
     * 下乡支援类型
     */
    private static final String RECORD_TYPE_RURAL_SUPPORT = "rural_support";

    @Resource
    private OutboundMapper outboundMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOutbound(OutboundSaveReqVO createReqVO) {
        OutboundDO outbound = buildOutbound(createReqVO);
        outboundMapper.insert(outbound);
        return outbound.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOutbound(OutboundSaveReqVO updateReqVO) {
        validateOutboundExists(updateReqVO.getId());
        OutboundDO update = buildOutbound(updateReqVO);
        outboundMapper.updateById(update);
    }

    @Override
    public OutboundRespVO getOutbound(Long id) {
        OutboundRespVO outbound = outboundMapper.selectByIdJoin(id);
        if (outbound == null) {
            throw exception(OUTBOUND_NOT_EXISTS);
        }
        return outbound;
    }

    @Override
    public PageResult<OutboundRespVO> getOutboundPage(OutboundPageReqVO pageReqVO) {
        return outboundMapper.selectPage(pageReqVO);
    }

    @Override
    public void deleteOutbound(Long id) {
        validateOutboundExists(id);
        outboundMapper.deleteById(id);
    }

    @Override
    public List<OutboundDO> getEffectiveOutboundListByPartnerId(Long partnerId) {
        return outboundMapper.selectEffectiveListByPartnerId(partnerId);
    }

    @Override
    public BigDecimal sumSupportYears(Long partnerId) {
        List<OutboundDO> list = getEffectiveOutboundListByPartnerId(partnerId);
        return list.stream()
                .filter(o -> RECORD_TYPE_RURAL_SUPPORT.equals(o.getRecordType()))
                .filter(o -> o.getSupportYears() != null)
                .map(OutboundDO::getSupportYears)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal sumContinuingEducationCredit(Long partnerId) {
        List<OutboundDO> list = getEffectiveOutboundListByPartnerId(partnerId);
        return list.stream()
                .filter(o -> !RECORD_TYPE_RURAL_SUPPORT.equals(o.getRecordType()))
                .filter(o -> o.getContinuingEducationCredit() != null)
                .map(OutboundDO::getContinuingEducationCredit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ========== 私有方法 ==========

    /**
     * 由 SaveReqVO 构造 DO，并计算天数 / 服务年限 / effective
     */
    private OutboundDO buildOutbound(OutboundSaveReqVO reqVO) {
        int durationDays = calcDurationDays(reqVO.getStartDate(), reqVO.getEndDate());
        OutboundDO outbound = BeanUtils.toBean(reqVO, OutboundDO.class)
                .setDurationDays(durationDays)
                .setEffective(1);
        if (RECORD_TYPE_RURAL_SUPPORT.equals(reqVO.getRecordType())) {
            outbound.setSupportYears(calcSupportYears(durationDays));
        }
        return outbound;
    }

    private OutboundDO validateOutboundExists(Long id) {
        OutboundDO outbound = outboundMapper.selectById(id);
        if (outbound == null) {
            throw exception(OUTBOUND_NOT_EXISTS);
        }
        return outbound;
    }

    /**
     * 计算外出天数：end - start + 1
     */
    private int calcDurationDays(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(start.toLocalDate(), end.toLocalDate()) + 1;
    }

    /**
     * 计算服务年限：天数 / 365，保留 1 位小数
     */
    private BigDecimal calcSupportYears(int durationDays) {
        if (durationDays <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(durationDays)
                .divide(BigDecimal.valueOf(365), 1, RoundingMode.HALF_UP);
    }

}
