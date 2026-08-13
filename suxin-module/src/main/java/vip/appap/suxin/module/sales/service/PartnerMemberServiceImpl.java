package vip.appap.suxin.module.sales.service;

import cn.hutool.core.collection.CollUtil;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerMemberGrantReqVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerMemberPageReqVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerMemberRespVO;
import vip.appap.suxin.module.partner.controller.app.vo.AppPartnerMemberConfigRespVO;
import vip.appap.suxin.module.partner.controller.app.vo.AppPartnerMemberRespVO;
import vip.appap.suxin.module.partner.convert.PartnerMemberConvert;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerDO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerMemberDO;
import vip.appap.suxin.module.partner.dal.dataobject.SalesOrderItemDO;
import vip.appap.suxin.module.partner.dal.mysql.PartnerMapper;
import vip.appap.suxin.module.partner.dal.mysql.PartnerMemberMapper;
import vip.appap.suxin.module.partner.enums.MemberSourceTypeEnum;
import vip.appap.suxin.module.partner.enums.MemberStatusEnum;
import vip.appap.suxin.module.partner.enums.MemberTypeEnum;
import vip.appap.suxin.module.partner.service.PartnerMemberService;
import vip.appap.suxin.module.product.dal.dataobject.ProductSkuDO;
import vip.appap.suxin.module.product.dal.dataobject.ProductSpuDO;
import vip.appap.suxin.module.product.dal.dataobject.ProductUnitDO;
import vip.appap.suxin.module.product.enums.ProductTypeEnum;
import vip.appap.suxin.module.product.service.ProductDisplayConfigService;
import vip.appap.suxin.module.product.service.ProductSkuService;
import vip.appap.suxin.module.product.service.ProductSpuService;
import vip.appap.suxin.module.product.service.ProductUnitService;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.List;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.module.partner.enums.ErrorCodeConstants.*;

@Service
@Validated
@Slf4j
public class PartnerMemberServiceImpl implements PartnerMemberService {

    private static final String MEMBER_DURATION_BASE_UNIT_SECOND = "秒";
    private static final int PRODUCT_UNIT_TYPE_TIME = 5;
    @Resource
    private PartnerMapper partnerMapper;
    @Resource
    private PartnerMemberMapper partnerMemberMapper;
    @Resource
    private ProductSkuService productSkuService;
    @Resource
    private ProductSpuService productSpuService;
    @Resource
    private ProductUnitService productUnitService;
    @Resource
    private ProductDisplayConfigService productDisplayConfigService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PartnerMemberDO activateMemberByOrderItem(Long userId, Integer memberType, BigDecimal durationQuantity,
                                                     Long durationUnitId, Long orderId, Long orderItemId, Long spuId,
                                                     Long skuId) {
        long durationSeconds = toDurationSeconds(durationQuantity, durationUnitId);
        PartnerMemberDO exists = partnerMemberMapper.selectByOrderItemId(orderItemId, memberType);
        if (exists != null) {
            return exists;
        }
        PartnerDO partner = partnerMapper.selectByIdForUpdate(userId);
        if (partner == null) {
            throw exception(PARTNER_NOT_EXISTS);
        }
        exists = partnerMemberMapper.selectByOrderItemId(orderItemId, memberType);
        if (exists != null) {
            return exists;
        }
        PartnerMemberDO member = buildMember(userId, memberType, durationQuantity, durationUnitId,
                durationSeconds, MemberSourceTypeEnum.ORDER.getType(), orderId, orderItemId, spuId, skuId, null);
        try {
            partnerMemberMapper.insert(member);
        } catch (DuplicateKeyException duplicateKeyException) {
            PartnerMemberDO duplicated = partnerMemberMapper.selectByOrderItemId(orderItemId, memberType);
            if (duplicated != null) {
                return duplicated;
            }
            throw duplicateKeyException;
        }
        updateProjection(userId, memberType);
        return member;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long grantMember(PartnerMemberGrantReqVO reqVO) {
        long durationSeconds = toDurationSeconds(reqVO.getDurationQuantity(), reqVO.getDurationUnitId());
        PartnerDO partner = partnerMapper.selectByIdForUpdate(reqVO.getUserId());
        if (partner == null) {
            throw exception(PARTNER_NOT_EXISTS);
        }
        PartnerMemberDO member = buildMember(reqVO.getUserId(), reqVO.getMemberType(), reqVO.getDurationQuantity(),
                reqVO.getDurationUnitId(), durationSeconds, MemberSourceTypeEnum.ADMIN_GRANT.getType(),
                null, null, null, null, reqVO.getRemark());
        partnerMemberMapper.insert(member);
        updateProjection(reqVO.getUserId(), reqVO.getMemberType());
        return member.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void activateMemberByPaidOrderItems(Long userId, List<SalesOrderItemDO> orderItems) {
        if (CollUtil.isEmpty(orderItems)) {
            return;
        }
        for (SalesOrderItemDO orderItem : orderItems) {
            MemberDurationSnapshot snapshot = getMemberOrderItemDuration(orderItem);
            if (snapshot == null) {
                continue;
            }
            activateMemberByOrderItem(userId, MemberTypeEnum.MARRIAGE_ADVANCED.getType(), snapshot.getDurationQuantity(),
                    snapshot.getDurationUnitId(), orderItem.getOrderId(), orderItem.getId(), orderItem.getSpuId(),
                    orderItem.getSkuId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revokeMemberByRefundedOrderItem(Long userId, SalesOrderItemDO orderItem) {
        MemberDurationSnapshot snapshot = getMemberOrderItemDuration(orderItem);
        if (snapshot == null) {
            return;
        }
        PartnerDO partner = partnerMapper.selectByIdForUpdate(userId);
        if (partner == null) {
            throw exception(PARTNER_NOT_EXISTS);
        }
        if (partner.getMemberExpireTime() == null) {
            return;
        }
        LocalDateTime newExpireTime;
        try {
            newExpireTime = partner.getMemberExpireTime().minusSeconds(
                    toDurationSeconds(snapshot.getDurationQuantity(), snapshot.getDurationUnitId()));
        } catch (DateTimeException ex) {
            throw exception(MEMBER_GRANT_DURATION_INVALID);
        }
        if (!newExpireTime.isAfter(LocalDateTime.now())) {
            updatePartnerMembership(partner.getId(), false, null);
            return;
        }
        updatePartnerMembership(partner.getId(), true, newExpireTime);
    }

    @Override
    public AppPartnerMemberRespVO getMyMembership(Long userId) {
        List<PartnerMemberDO> members = partnerMemberMapper.selectActiveList(userId);
        PartnerDO partner = partnerMapper.selectById(userId);
        AppPartnerMemberRespVO respVO = new AppPartnerMemberRespVO();
        respVO.setMembers(members.stream().map(this::buildAppMember).toList());
        PartnerMemberDO latestActive = CollUtil.isNotEmpty(members) ? members.get(0) : null;
        if (latestActive != null) {
            respVO.setActive(true);
            respVO.setMemberType(latestActive.getMemberType());
            respVO.setMemberExpireTime(latestActive.getEndTime());
            return respVO;
        }
        if (partner == null || !Boolean.TRUE.equals(partner.getIsMember())
                || partner.getMemberExpireTime() == null
                || !partner.getMemberExpireTime().isAfter(LocalDateTime.now())) {
            respVO.setActive(false);
            respVO.setMemberType(0);
            return respVO;
        }
        respVO.setActive(true);
        respVO.setMemberType(MemberTypeEnum.MARRIAGE_ADVANCED.getType());
        respVO.setMemberExpireTime(partner.getMemberExpireTime());
        return respVO;
    }

    @Override
    public AppPartnerMemberConfigRespVO getMemberConfig() {
        Long memberCategoryId = getConfiguredMemberDisplayCategoryId();
        if (memberCategoryId == null) {
            throw exception(MEMBER_CONFIG_NOT_FOUND);
        }
        AppPartnerMemberConfigRespVO respVO = new AppPartnerMemberConfigRespVO();
        respVO.setMemberCategoryId(memberCategoryId);
        respVO.setMemberType(MemberTypeEnum.MARRIAGE_ADVANCED.getType());
        return respVO;
    }

    private Long getConfiguredMemberDisplayCategoryId() {
        List<Long> categoryIds = productDisplayConfigService.getEnabledCategoryIdsBySceneCode(
                ProductDisplayConfigService.SCENE_MEMBER_PAGE);
        if (CollUtil.isNotEmpty(categoryIds)) {
            return categoryIds.get(0);
        }
        return null;
    }

    @Override
    public PageResult<PartnerMemberRespVO> getMemberPage(PartnerMemberPageReqVO pageReqVO) {
        return PartnerMemberConvert.INSTANCE.convertPage(partnerMemberMapper.selectPage(pageReqVO));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int expireMembers() {
        int count = 0;
        while (true) {
            List<PartnerDO> partners = partnerMapper.selectExpiredMemberList(200);
            if (CollUtil.isEmpty(partners)) {
                break;
            }
            for (PartnerDO partner : partners) {
                updatePartnerMembership(partner.getId(), false, null);
                count++;
            }
        }
        return count;
    }

    @Override
    public void createMember(Long partnerId, String ip, Integer terminal) {
        PartnerDO partner = partnerMapper.selectById(partnerId);
        if (partner == null) {
            throw exception(PARTNER_NOT_EXISTS);
        }
        PartnerDO updateObj = new PartnerDO();
        updateObj.setId(partnerId);
        updateObj.setIsMember(true);
        updateObj.setRegisterIp(ip);
        updateObj.setRegisterTerminal(terminal);
        partnerMapper.updateById(updateObj);
    }

    @Override
    public String getMemberUserMobile(Long id) {
        PartnerDO partner = partnerMapper.selectById(id);
        return partner != null ? partner.getMobile() : null;
    }

    @Override
    public String getMemberUserEmail(Long id) {
        PartnerDO partner = partnerMapper.selectById(id);
        return partner != null ? partner.getEmail() : null;
    }

    private PartnerMemberDO buildMember(Long userId, Integer memberType, BigDecimal durationQuantity,
                                        Long durationUnitId, long durationSeconds, Integer sourceType,
                                        Long orderId, Long orderItemId,
                                        Long spuId, Long skuId, String remark) {
        PartnerMemberDO latestActive = partnerMemberMapper.selectLatestActive(userId, memberType);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = latestActive != null && latestActive.getEndTime().isAfter(now)
                ? latestActive.getEndTime() : now;
        PartnerMemberDO member = new PartnerMemberDO();
        member.setUserId(userId);
        member.setMemberType(memberType);
        member.setStartTime(startTime);
        try {
            member.setEndTime(startTime.plusSeconds(durationSeconds));
        } catch (DateTimeException ex) {
            throw exception(MEMBER_GRANT_DURATION_INVALID);
        }
        member.setStatus(MemberStatusEnum.ACTIVE.getStatus());
        member.setSourceType(sourceType);
        member.setOrderId(orderId);
        member.setOrderItemId(orderItemId);
        member.setSpuId(spuId);
        member.setSkuId(skuId);
        member.setDurationQuantity(durationQuantity);
        member.setDurationUnitId(durationUnitId);
        member.setRemark(remark);
        return member;
    }

    private AppPartnerMemberRespVO.Member buildAppMember(PartnerMemberDO member) {
        AppPartnerMemberRespVO.Member respVO = new AppPartnerMemberRespVO.Member();
        respVO.setId(member.getId());
        respVO.setMemberType(member.getMemberType());
        respVO.setStartTime(member.getStartTime());
        respVO.setEndTime(member.getEndTime());
        respVO.setSourceType(member.getSourceType());
        respVO.setDurationQuantity(member.getDurationQuantity());
        respVO.setDurationUnitId(member.getDurationUnitId());
        return respVO;
    }

    private void updateProjection(Long userId, Integer memberType) {
        PartnerMemberDO active = partnerMemberMapper.selectLatestActive(userId, memberType);
        updatePartnerMembership(userId, active != null, active != null ? active.getEndTime() : null);
    }

    private MemberDurationSnapshot getMemberOrderItemDuration(SalesOrderItemDO orderItem) {
        if (orderItem == null || orderItem.getSkuId() == null || orderItem.getSpuId() == null) {
            return null;
        }
        ProductSpuDO spu = productSpuService.getSpu(orderItem.getSpuId(), true);
        if (spu == null || !isMemberSpu(spu)) {
            return null;
        }
        ProductSkuDO sku = productSkuService.getSku(orderItem.getSkuId(), true);
        if (sku == null || sku.getQuantity() == null) {
            throw exception(MEMBER_GRANT_DURATION_INVALID);
        }
        Integer count = orderItem.getCount();
        if (count == null || count <= 0) {
            throw exception(MEMBER_GRANT_DURATION_INVALID);
        }
        BigDecimal durationQuantity = sku.getQuantity().multiply(BigDecimal.valueOf(count));
        toDurationSeconds(durationQuantity, spu.getUnitId());
        return new MemberDurationSnapshot(durationQuantity, spu.getUnitId());
    }

    private boolean isMemberSpu(ProductSpuDO spu) {
        return ProductTypeEnum.isMember(spu.getType());
    }

    private long toDurationSeconds(BigDecimal durationQuantity, Long durationUnitId) {
        if (durationQuantity == null || durationQuantity.signum() <= 0 || durationUnitId == null) {
            throw exception(MEMBER_GRANT_DURATION_INVALID);
        }
        ProductUnitDO unit = productUnitService.getUnit(durationUnitId);
        if (unit == null || !CommonStatusEnum.ENABLE.getStatus().equals(unit.getStatus())) {
            throw exception(MEMBER_GRANT_DURATION_INVALID);
        }
        BigDecimal factor;
        if (MEMBER_DURATION_BASE_UNIT_SECOND.equals(unit.getName())
                && (Integer.valueOf(0).equals(unit.getType()) || isBlank(unit.getRelativeFactor()))) {
            factor = BigDecimal.ONE;
        } else if (Integer.valueOf(PRODUCT_UNIT_TYPE_TIME).equals(unit.getType())) {
            if (isBlank(unit.getRelativeFactor())) {
                throw exception(MEMBER_GRANT_DURATION_INVALID);
            }
            try {
                factor = new BigDecimal(unit.getRelativeFactor());
            } catch (NumberFormatException ex) {
                throw exception(MEMBER_GRANT_DURATION_INVALID);
            }
            if (factor.signum() <= 0) {
                throw exception(MEMBER_GRANT_DURATION_INVALID);
            }
        } else {
            throw exception(MEMBER_GRANT_DURATION_INVALID);
        }
        try {
            long durationSeconds = durationQuantity.multiply(factor).toBigIntegerExact().longValueExact();
            if (durationSeconds <= 0) {
                throw exception(MEMBER_GRANT_DURATION_INVALID);
            }
            return durationSeconds;
        } catch (ArithmeticException ex) {
            throw exception(MEMBER_GRANT_DURATION_INVALID);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void updatePartnerMembership(Long partnerId, Boolean isMember, LocalDateTime memberExpireTime) {
        PartnerDO updateObj = new PartnerDO();
        updateObj.setId(partnerId);
        updateObj.setIsMember(isMember);
        updateObj.setMemberExpireTime(memberExpireTime);
        partnerMapper.updateById(updateObj);
    }

    @Data
    @AllArgsConstructor
    private static class MemberDurationSnapshot {

        private BigDecimal durationQuantity;

        private Long durationUnitId;

    }

}
