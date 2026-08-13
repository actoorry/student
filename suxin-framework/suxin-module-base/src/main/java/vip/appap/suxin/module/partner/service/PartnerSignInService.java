package vip.appap.suxin.module.partner.service;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerSignInConfigSaveReqVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerSignInRecordPageReqVO;
import vip.appap.suxin.module.partner.controller.app.vo.AppPartnerSignInSummaryRespVO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerSignInConfigDO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerSignInRecordDO;

import java.util.List;

/**
 * 会员签到 Service 接口
 *
 * @author 书心软件
 */
public interface PartnerSignInService {

    /**
     * 保存签到配置
     *
     * @param saveReqVO 配置信息
     */
    void saveSignInConfig(PartnerSignInConfigSaveReqVO saveReqVO);

    /**
     * 批量保存签到配置
     *
     * @param saveReqVOList 配置列表
     */
    void saveSignInConfigList(List<PartnerSignInConfigSaveReqVO> saveReqVOList);

    /**
     * 获得签到配置列表
     *
     * @return 签到配置列表
     */
    List<PartnerSignInConfigDO> getSignInConfigList();

    /**
     * 鑾峰緱鍚敤鐨勭鍒伴厤缃?
     *
     * @return 绛惧埌閰嶇疆鍒楄〃
     */
    List<PartnerSignInConfigDO> getEnabledSignInConfigList();

    /**
     * 获得签到记录分页
     *
     * @param pageReqVO 分页查询
     * @return 签到记录分页
     */
    PageResult<PartnerSignInRecordDO> getSignInRecordPage(PartnerSignInRecordPageReqVO pageReqVO);

    /**
     * 会员签到
     *
     * @param userId 用户编号
     * @return 签到记录
     */
    PartnerSignInRecordDO signIn(Long userId);

    /**
     * 鑾峰緱绛惧埌鐘舵€佹憳瑕?
     *
     * @param userId 鐢ㄦ埛缂栧彿
     * @return 绛惧埌鐘舵€佹憳瑕?
     */
    AppPartnerSignInSummaryRespVO getSignInSummary(Long userId);

    /**
     * 获得用户今日签到记录
     *
     * @param userId 用户编号
     * @return 签到记录
     */
    PartnerSignInRecordDO getTodaySignInRecord(Long userId);

}
