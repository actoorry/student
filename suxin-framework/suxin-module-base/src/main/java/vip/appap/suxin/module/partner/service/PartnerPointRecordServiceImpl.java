package vip.appap.suxin.module.partner.service;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerPointRecordPageReqVO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerDO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerPointRecordDO;
import vip.appap.suxin.module.partner.dal.mysql.PartnerPointRecordMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.module.partner.enums.ErrorCodeConstants.PARTNER_NOT_EXISTS;
import static vip.appap.suxin.module.partner.enums.ErrorCodeConstants.PARTNER_POINT_NOT_ENOUGH;

/**
 * 会员积分记录 Service 实现类
 *
 * @author 书心软件
 */
@Service
@Validated
public class PartnerPointRecordServiceImpl implements PartnerPointRecordService {

    @Resource
    private PartnerPointRecordMapper partnerPointRecordMapper;

    @Resource
    private PartnerService partnerService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PartnerPointRecordDO createPointRecord(Long userId, Integer point, Integer bizType, String bizId) {
        // 校验会员存在
        PartnerDO member = partnerService.getPartner(userId);
        if (member == null) {
            throw exception(PARTNER_NOT_EXISTS);
        }

        // 计算积分
        Integer totalPoint = member.getPoint() + point;
        if (totalPoint < 0) {
            throw exception(PARTNER_POINT_NOT_ENOUGH);
        }

        // 创建积分记录
        PartnerPointRecordDO record = PartnerPointRecordDO.builder()
                .userId(userId)
                .point(point)
                .totalPoint(totalPoint)
                .bizType(bizType)
                .bizId(bizId)
                .title("积分变动")
                .description("积分变动 " + (point > 0 ? "+" : "") + point)
                .build();
        partnerPointRecordMapper.insert(record);

        // 更新会员积分
        partnerService.updatePartnerPoint(userId, totalPoint);

        return record;
    }

    @Override
    public PageResult<PartnerPointRecordDO> getPointRecordPage(PartnerPointRecordPageReqVO pageReqVO) {
        return partnerPointRecordMapper.selectPage(pageReqVO);
    }

}
