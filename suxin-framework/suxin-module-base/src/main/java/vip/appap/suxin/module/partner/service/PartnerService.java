package vip.appap.suxin.module.partner.service;

import jakarta.validation.Valid;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.partner.controller.admin.vo.*;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerSalesPageReqVO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerDO;
import vip.appap.suxin.module.partner.service.bo.PartnerSalesCreateReqBO;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 合作伙伴 Service 接口
 *
 * @author 书心软件
 */
public interface PartnerService {

    /**
     * 通过手机查询用户
     *
     * @param mobile 手机
     * @return 用户对象
     */
    PartnerDO getPartnerByMobile(String mobile);

    /**
     * 基于用户昵称，模糊匹配用户列表
     *
     * @param nickname 用户昵称，模糊匹配
     * @return 用户信息的列表
     */
    List<PartnerDO> getPartnerListByNickname(String nickname);

    /**
     * 通过用户 ID 查询用户
     *
     * @param id 用户ID
     * @return 用户对象信息
     */
    PartnerDO getPartner(Long id);

    /**
     * 通过用户 ID 查询用户们
     *
     * @param ids 用户 ID
     * @return 用户对象信息数组
     */
    List<PartnerDO> getPartnerList(Collection<Long> ids);

    /**
     * 【管理员】创建合作伙伴
     *
     * @param createReqVO 创建信息
     * @return 用户编号
     */
    Long createPartner(@Valid PartnerCreateReqVO createReqVO);

    /**
     * 【管理员】更新合作伙伴
     *
     * @param updateReqVO 更新信息
     */
    void updatePartner(@Valid PartnerUpdateReqVO updateReqVO);

    /**
     * 更新用户基本信息（昵称、头像）
     *
     * @param userId 用户编号
     * @param nickname 昵称
     * @param avatar 头像
     */
    void updatePartnerProfile(Long userId, String nickname, String avatar);

    /**
     * 修改用户手机号
     *
     * @param userId 用户编号
     * @param mobile 新手机号
     * @param code 短信验证码
     */
    void updatePartnerMobile(Long userId, String mobile, String code);

    /**
     * 基于微信授权码修改手机号
     *
     * @param userId 用户编号
     * @param code 微信授权码
     */
    void updatePartnerMobileByWeixin(Long userId, String code);

    /**
     * 修改用户密码
     *
     * @param userId 用户编号
     * @param code 短信验证码
     * @param newPassword 新密码
     */
    void updatePartnerPassword(Long userId, String code, String newPassword);

    /**
     * 重置用户密码
     *
     * @param mobile 手机号
     * @param code 短信验证码
     * @param newPassword 新密码
     */
    void resetPartnerPassword(String mobile, String code, String newPassword);

    /**
     * 【管理员】删除合作伙伴
     *
     * @param id 用户编号
     */
    void deletePartner(Long id);

    /**
     * 【管理员】获得合作伙伴分页
     *
     * @param pageReqVO 分页查询
     * @return 合作伙伴分页
     */
    PageResult<PartnerDO> getPartnerPage(PartnerPageReqVO pageReqVO);

    /**
     * 获取未绑定系统用户的合作伙伴列表
     *
     * @return 未绑定系统用户的合作伙伴列表
     */
    List<PartnerDO> getPartnerListWithoutUser();

    /**
     * 获取用户 Map
     *
     * @param ids 用户 ID 集合
     * @return 用户 Map，key 为用户 ID，value 为用户对象
     */
    Map<Long, PartnerDO> getPartnerMap(Collection<Long> ids);

    /**
     * 获取当前登录用户对应的合作伙伴
     *
     * @return 合作伙伴对象，未登录返回 null
     */
    PartnerDO getCurrentPartner();

    /**
     * 校验邮箱在 partner 表中唯一
     *
     * @param id    用户编号（排除自身）
     * @param email 邮箱
     */
    void validateEmailUnique(Long id, String email);

    /**
     * 校验手机号在 partner 表中唯一
     *
     * @param id     用户编号（排除自身）
     * @param mobile 手机号
     */
    void validateMobileUnique(Long id, String mobile);

    /**
     * 初始化合作伙伴会员信息
     */
    void initPartnerMember(Long partnerId, String registerIp, Integer registerTerminal);

    /**
     * 【管理员】获得会员分页
     */
    PageResult<PartnerDO> getPartnerMemberPage(PartnerMemberPageReqVO pageReqVO);

    /**
     * 更新会员信息（等级、分组、标签）
     */
    void updatePartnerMember(PartnerMemberUpdateReqVO updateReqVO);

    void updatePartnerPoint(Long id, Integer point);

    void updatePartnerLevel(Long id, Long levelId);

    void updatePartnerExperience(Long id, Integer experience);

    void updatePartnerGroup(Long id, Long groupId);

    void updatePartnerTagIds(Long id, String tagIds);

    Long getPartnerCountByGroupId(Long groupId);

    Long getPartnerCountByLevelId(Long levelId);

    Long getPartnerCountByTagId(Long tagId);

    // ==================== CRM 客户业务方法（原 PartnerSalesService） ====================

    /**
     * 新增客户（CRM 创建用）
     *
     * @param createReqVO 客户信息
     * @param userId      操作人
     * @return 客户编号
     */
    Long createPartner(@Valid PartnerSalesSaveReqVO createReqVO, Long userId);

    /**
     * 新增客户（BO 方式，线索转化用）
     *
     * @param createReqBO 客户信息
     * @param userId      操作人
     * @return 客户编号
     */
    Long createPartner(PartnerSalesCreateReqBO createReqBO, Long userId);

    /**
     * 新增客户（BO 方式 + 指定 partnerId，线索转化共享主键用）
     *
     * @param createReqBO 客户信息
     * @param userId      操作人
     * @param partnerId   partner 编号
     * @return 客户编号
     */
    Long createPartner(PartnerSalesCreateReqBO createReqBO, Long userId, Long partnerId);

    /**
     * 更新客户（CRM 业务字段）
     */
    void updatePartner(@Valid PartnerSalesSaveReqVO updateReqVO);

    /**
     * 更新客户成交状态
     */
    void updatePartnerDealStatus(Long id, Boolean dealStatus);

    /**
     * 更新客户跟进信息
     */
    void updatePartnerFollowUp(Long id, LocalDateTime contactNextTime, String contactLastContent);

    /**
     * 转移客户
     */
    void transferPartner(PartnerSalesTransferReqVO reqVO, Long userId);

    /**
     * 锁定/解锁客户
     */
    void lockPartner(PartnerSalesLockReqVO lockReqVO, Long userId);

    /**
     * CRM 客户分页（含数据权限、公海判断）
     */
    PageResult<PartnerDO> getPartnerSalesPage(PartnerSalesPageReqVO pageReqVO, Long userId);

    /**
     * 待进入公海客户分页
     */
    PageResult<PartnerDO> getPutPoolRemindPartnerPage(PartnerSalesPageReqVO pageVO, Long userId);

    /**
     * 待进入公海客户数量
     */
    Long getPutPoolRemindPartnerCount(Long userId);

    /**
     * 今日需联系客户数量
     */
    Long getTodayContactPartnerCount(Long userId);

    /**
     * 待跟进客户数量
     */
    Long getFollowPartnerCount(Long userId);

    /**
     * 校验客户是否存在
     */
    void validatePartner(Long id);

    /**
     * 校验合作伙伴是否存在且启用
     *
     * @param id 合作伙伴编号
     * @return 合作伙伴对象
     */
    PartnerDO validatePartnerExistsAndEnable(Long id);

    // ==================== 公海相关操作 ====================

    /**
     * 客户放入公海
     */
    void putPartnerPool(Long id);

    /**
     * 领取公海客户
     */
    void receivePartner(List<Long> ids, Long ownerUserId, Boolean isReceive);

    /**
     * 自动回收公海客户
     */
    int autoPutPartnerPool();

    // ==================== 导入相关操作 ====================

    /**
     * 导入客户
     */
    PartnerSalesImportRespVO importPartnerList(List<PartnerSalesImportExcelVO> importPartners,
                                               PartnerSalesImportReqVO importReqVO);

}
