package vip.appap.suxin.module.partner.dal.mysql;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjUtil;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.framework.mybatis.core.query.MPJLambdaWrapperX;

import vip.appap.suxin.module.partner.controller.admin.vo.PartnerMemberPageReqVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerPageReqVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerSalesPageReqVO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerDO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerSalesPoolConfigDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import vip.appap.suxin.module.partner.enums.CrmBizTypeEnum;
import vip.appap.suxin.module.partner.enums.CrmSceneTypeEnum;
import vip.appap.suxin.module.partner.util.CrmPermissionUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 合作伙伴 Mapper
 *
 * @author 书心软件
 */
@Mapper
public interface PartnerMapper extends BaseMapperX<PartnerDO> {

    default PartnerDO selectByMobile(String mobile) {
        return selectOne(PartnerDO::getMobile, mobile);
    }

    default PartnerDO selectByEmail(String email) {
        return selectOne(PartnerDO::getEmail, email);
    }

    @Select("SELECT * FROM partner WHERE id = #{id} AND deleted = 0 FOR UPDATE")
    PartnerDO selectByIdForUpdate(@Param("id") Long id);

    default List<PartnerDO> selectExpiredMemberList(Integer limit) {
        return selectList(new LambdaQueryWrapperX<PartnerDO>()
                .eq(PartnerDO::getIsMember, true)
                .and(wrapper -> wrapper.lt(PartnerDO::getMemberExpireTime, LocalDateTime.now())
                        .or().isNull(PartnerDO::getMemberExpireTime))
                .last("LIMIT " + limit));
    }

    default List<PartnerDO> selectListByNicknameLike(String nickname) {
        return selectList(new LambdaQueryWrapperX<PartnerDO>()
                .likeIfPresent(PartnerDO::getNickname, nickname));
    }

    default PageResult<PartnerDO> selectPage(PartnerPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PartnerDO>()
                .likeIfPresent(PartnerDO::getMobile, reqVO.getMobile())
                .likeIfPresent(PartnerDO::getNickname, reqVO.getNickname())
                .betweenIfPresent(PartnerDO::getCreateTime, reqVO.getCreateTime())
                .eqIfPresent(PartnerDO::getIsCustomer, reqVO.getCustomer())
                .eqIfPresent(PartnerDO::getIsSupplier, reqVO.getSupplier())
                .eqIfPresent(PartnerDO::getIsCompany, reqVO.getCompany())
                .orderByDesc(PartnerDO::getId));
    }

    /**
     * 查询未绑定系统用户的 partner 列表
     *
     * @return 未绑定系统的 partner 列表
     */
    default List<PartnerDO> selectListWithoutUser() {
        return selectList(new LambdaQueryWrapperX<PartnerDO>()
                .notExists("SELECT 1 FROM system_users u WHERE u.id = partner.id"));
    }

    default PageResult<PartnerDO> selectMemberPage(PartnerMemberPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PartnerDO>()
                .eq(PartnerDO::getIsMember, true)
                .likeIfPresent(PartnerDO::getMobile, reqVO.getMobile())
                .likeIfPresent(PartnerDO::getNickname, reqVO.getNickname())
                .eqIfPresent(PartnerDO::getCustomerLevel, reqVO.getCustomerLevel())
                .eqIfPresent(PartnerDO::getGroupId, reqVO.getGroupId())
                .betweenIfPresent(PartnerDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(PartnerDO::getId));
    }

    @Select("SELECT COUNT(*) FROM partner WHERE group_id = #{groupId} AND deleted = 0")
    Long selectCountByGroupId(@Param("groupId") Long groupId);

    @Select("SELECT COUNT(*) FROM partner WHERE customer_level = #{customerLevel} AND deleted = 0")
    Long selectCountByCustomerLevel(@Param("customerLevel") Long customerLevel);

    @Select("SELECT COUNT(*) FROM partner WHERE tag_ids LIKE CONCAT('%', #{tagId}, '%') AND deleted = 0")
    Long selectCountByTagId(@Param("tagId") Long tagId);

    // ==================== CRM 客户查询方法（原 PartnerSalesMapper） ====================

    default Long selectCountByLockStatusAndOwnerUserId(Boolean lockStatus, Long ownerUserId) {
        return selectCount(new LambdaUpdateWrapper<PartnerDO>()
                .eq(PartnerDO::getLockStatus, lockStatus)
                .eq(PartnerDO::getOwnerUserId, ownerUserId));
    }

    default Long selectCountByDealStatusAndOwnerUserId(@Nullable Boolean dealStatus, Long ownerUserId) {
        return selectCount(new LambdaQueryWrapperX<PartnerDO>()
                .eqIfPresent(PartnerDO::getDealStatus, dealStatus)
                .eq(PartnerDO::getOwnerUserId, ownerUserId));
    }

    default int updateOwnerUserIdById(Long id, Long ownerUserId) {
        return update(new LambdaUpdateWrapper<PartnerDO>()
                .eq(PartnerDO::getId, id)
                .set(PartnerDO::getOwnerUserId, ownerUserId));
    }

    /**
     * CRM 客户分页查询（含数据权限、公海判断）
     */
    default PageResult<PartnerDO> selectSalesPage(PartnerSalesPageReqVO pageReqVO, Long ownerUserId) {
        MPJLambdaWrapperX<PartnerDO> query = new MPJLambdaWrapperX<>();
        // 拼接数据权限的查询条件
        if (Boolean.TRUE.equals(pageReqVO.getPool())) {
            query.isNull(PartnerDO::getOwnerUserId);
        } else {
            CrmPermissionUtils.appendPermissionCondition(query, CrmBizTypeEnum.CRM_CUSTOMER.getType(),
                    PartnerDO::getId, ownerUserId, pageReqVO.getSceneType());
        }
        // 拼接自身的查询条件（所有字段已在 partner 表，无需 JOIN）
        query.selectAll(PartnerDO.class)
                .likeIfPresent(PartnerDO::getName, pageReqVO.getName())
                .eqIfPresent(PartnerDO::getMobile, pageReqVO.getMobile())
                .eqIfPresent(PartnerDO::getIndustryId, pageReqVO.getIndustryId())
                .eqIfPresent(PartnerDO::getSalesLevel, pageReqVO.getLevel())
                .eqIfPresent(PartnerDO::getSalesSource, pageReqVO.getSource())
                .eqIfPresent(PartnerDO::getFollowUpStatus, pageReqVO.getFollowUpStatus());

        // backlog 查询
        if (ObjUtil.isNotNull(pageReqVO.getContactStatus())) {
            Assert.isNull(pageReqVO.getPool(), "pool 必须是 null");
            LocalDateTime beginOfToday = LocalDateTimeUtil.beginOfDay(LocalDateTime.now());
            LocalDateTime endOfToday = LocalDateTimeUtil.endOfDay(LocalDateTime.now());
            if (pageReqVO.getContactStatus().equals(PartnerSalesPageReqVO.CONTACT_TODAY)) {
                query.between(PartnerDO::getContactNextTime, beginOfToday, endOfToday);
            } else if (pageReqVO.getContactStatus().equals(PartnerSalesPageReqVO.CONTACT_EXPIRED)) {
                query.lt(PartnerDO::getContactNextTime, beginOfToday);
            } else if (pageReqVO.getContactStatus().equals(PartnerSalesPageReqVO.CONTACT_ALREADY)) {
                query.between(PartnerDO::getContactLastTime, beginOfToday, endOfToday);
            } else {
                throw new IllegalArgumentException("未知联系状态：" + pageReqVO.getContactStatus());
            }
        }
        return selectPage(pageReqVO, query);
    }

    default PartnerDO selectByPartnerName(String name) {
        return selectOne(PartnerDO::getName, name);
    }

    default PageResult<PartnerDO> selectPutPoolRemindPartnerPage(PartnerSalesPageReqVO pageReqVO,
                                                                  PartnerSalesPoolConfigDO poolConfig,
                                                                  Long ownerUserId) {
        final MPJLambdaWrapperX<PartnerDO> query = buildPutPoolRemindQuery(pageReqVO, poolConfig, ownerUserId);
        return selectPage(pageReqVO, query.selectAll(PartnerDO.class));
    }

    default Long selectPutPoolRemindPartnerCount(PartnerSalesPageReqVO pageReqVO,
                                                  PartnerSalesPoolConfigDO poolConfig,
                                                  Long userId) {
        final MPJLambdaWrapperX<PartnerDO> query = buildPutPoolRemindQuery(pageReqVO, poolConfig, userId);
        return selectCount(query);
    }

    private MPJLambdaWrapperX<PartnerDO> buildPutPoolRemindQuery(PartnerSalesPageReqVO pageReqVO,
                                                                  PartnerSalesPoolConfigDO poolConfig,
                                                                  Long ownerUserId) {
        MPJLambdaWrapperX<PartnerDO> query = new MPJLambdaWrapperX<>();
        CrmPermissionUtils.appendPermissionCondition(query, CrmBizTypeEnum.CRM_CUSTOMER.getType(),
                PartnerDO::getId, ownerUserId, pageReqVO.getSceneType());
        query.eq(PartnerDO::getLockStatus, false).eq(PartnerDO::getDealStatus, false);
        Integer dealExpireDays = poolConfig.getDealExpireDays();
        LocalDateTime startDealRemindTime = LocalDateTime.now().minusDays(dealExpireDays);
        LocalDateTime endDealRemindTime = LocalDateTime.now()
                .minusDays(Math.max(dealExpireDays - poolConfig.getNotifyDays(), 0));
        Integer contactExpireDays = poolConfig.getContactExpireDays();
        LocalDateTime startContactRemindTime = LocalDateTime.now().minusDays(contactExpireDays);
        LocalDateTime endContactRemindTime = LocalDateTime.now()
                .minusDays(Math.max(contactExpireDays - poolConfig.getNotifyDays(), 0));
        query.and(q -> {
            q.between(PartnerDO::getOwnerTime, startDealRemindTime, endDealRemindTime)
            .or(w -> w.between(PartnerDO::getOwnerTime, startContactRemindTime, endContactRemindTime)
                    .and(p -> p.between(PartnerDO::getContactLastTime, startContactRemindTime, endContactRemindTime)
                            .or().isNull(PartnerDO::getContactLastTime)));
        });
        return query;
    }

    /**
     * 获得需要过期到公海的客户列表
     */
    default List<PartnerDO> selectListByAutoPool(PartnerSalesPoolConfigDO poolConfig) {
        LambdaQueryWrapper<PartnerDO> query = new LambdaQueryWrapper<>();
        query.gt(PartnerDO::getOwnerUserId, 0);
        query.eq(PartnerDO::getLockStatus, false).eq(PartnerDO::getDealStatus, false);
        LocalDateTime dealExpireTime = LocalDateTime.now().minusDays(poolConfig.getDealExpireDays());
        LocalDateTime contactExpireTime = LocalDateTime.now().minusDays(poolConfig.getContactExpireDays());
        query.and(q -> {
            q.lt(PartnerDO::getOwnerTime, dealExpireTime)
            .or(w -> w.lt(PartnerDO::getOwnerTime, contactExpireTime)
                    .and(p -> p.lt(PartnerDO::getContactLastTime, contactExpireTime)
                            .or().isNull(PartnerDO::getContactLastTime)));
        });
        return selectList(query);
    }

    default Long selectCountByTodayContact(Long ownerUserId) {
        MPJLambdaWrapperX<PartnerDO> query = new MPJLambdaWrapperX<>();
        CrmPermissionUtils.appendPermissionCondition(query, CrmBizTypeEnum.CRM_CUSTOMER.getType(),
                PartnerDO::getId, ownerUserId, CrmSceneTypeEnum.OWNER.getType());
        LocalDateTime beginOfToday = LocalDateTimeUtil.beginOfDay(LocalDateTime.now());
        LocalDateTime endOfToday = LocalDateTimeUtil.endOfDay(LocalDateTime.now());
        query.between(PartnerDO::getContactNextTime, beginOfToday, endOfToday);
        return selectCount(query);
    }

    default Long selectCountByFollow(Long ownerUserId) {
        MPJLambdaWrapperX<PartnerDO> query = new MPJLambdaWrapperX<>();
        CrmPermissionUtils.appendPermissionCondition(query, CrmBizTypeEnum.CRM_CUSTOMER.getType(),
                PartnerDO::getId, ownerUserId, CrmSceneTypeEnum.OWNER.getType());
        query.eq(PartnerDO::getFollowUpStatus, false);
        return selectCount(query);
    }

}
