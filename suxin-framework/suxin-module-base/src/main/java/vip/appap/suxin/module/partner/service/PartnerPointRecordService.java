package vip.appap.suxin.module.partner.service;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerPointRecordPageReqVO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerPointRecordDO;

/**
 * 会员积分记录 Service 接口
 *
 * @author 书心软件
 */
public interface PartnerPointRecordService {

    /**
     * 创建积分记录
     *
     * @param userId 用户编号
     * @param point 积分
     * @param bizType 业务类型
     * @param bizId 业务编号
     * @return 积分记录
     */
    PartnerPointRecordDO createPointRecord(Long userId, Integer point, Integer bizType, String bizId);

    /**
     * 获得积分记录分页
     *
     * @param pageReqVO 分页查询
     * @return 积分记录分页
     */
    PageResult<PartnerPointRecordDO> getPointRecordPage(PartnerPointRecordPageReqVO pageReqVO);

}
