package vip.appap.suxin.module.sales.service;

import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.product.api.ProductCategoryApi;
import vip.appap.suxin.module.product.api.ProductSpuApi;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesCouponTemplateCreateReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesCouponTemplatePageReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesCouponTemplateUpdateReqVO;
import vip.appap.suxin.module.sales.convert.SalesCouponTemplateConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesCouponTemplateDO;
import vip.appap.suxin.module.sales.dal.mysql.SalesCouponTemplateMapper;
import vip.appap.suxin.module.sales.enums.SalesProductScopeEnum;
import vip.appap.suxin.module.sales.enums.SalesCouponTakeTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.module.sales.enums.SalesErrorCodeConstants.*;

/**
 * 优惠劵模板 Service 实现类
 *
 * @author 书心软件
 */
@Service
@Validated
public class SalesCouponTemplateServiceImpl implements SalesCouponTemplateService {

    @Resource
    private SalesCouponTemplateMapper couponTemplateMapper;

    @Resource
    private ProductCategoryApi productCategoryApi;
    @Resource
    private ProductSpuApi productSpuApi;

    @Override
    public boolean isTakeLimitCountUnlimited(Integer takeLimitCount) {
        return SalesCouponTemplateDO.TAKE_LIMIT_COUNT_MAX.equals(takeLimitCount);
    }

    @Override
    public boolean isTotalCountUnlimited(Integer totalCount) {
        return SalesCouponTemplateDO.TOTAL_COUNT_MAX.equals(totalCount);
    }

    @Override
    public Long createCouponTemplate(SalesCouponTemplateCreateReqVO createReqVO) {
        // 校验商品范围
        validateProductScope(createReqVO.getProductScope(), createReqVO.getProductScopeValues());
        // 插入
        SalesCouponTemplateDO couponTemplate = SalesCouponTemplateConvert.INSTANCE.convert(createReqVO)
                .setStatus(CommonStatusEnum.ENABLE.getStatus());
        couponTemplateMapper.insert(couponTemplate);
        // 返回
        return couponTemplate.getId();
    }

    @Override
    public void updateCouponTemplate(SalesCouponTemplateUpdateReqVO updateReqVO) {
        // 校验存在
        SalesCouponTemplateDO couponTemplate = validateCouponTemplateExists(updateReqVO.getId());
        // 校验发放数量不能过小（仅在 SalesCouponTakeTypeEnum.USER 用户领取时）
        if (SalesCouponTakeTypeEnum.isUser(couponTemplate.getTakeType())
                && !isTotalCountUnlimited(updateReqVO.getTotalCount()) // 非不限制总发放数量
                && updateReqVO.getTotalCount() < couponTemplate.getTakeCount()) {
            throw exception(COUPON_TEMPLATE_TOTAL_COUNT_TOO_SMALL, couponTemplate.getTakeCount());
        }
        // 校验商品范围
        validateProductScope(updateReqVO.getProductScope(), updateReqVO.getProductScopeValues());

        // 更新
        SalesCouponTemplateDO updateObj = SalesCouponTemplateConvert.INSTANCE.convert(updateReqVO);
        couponTemplateMapper.updateById(updateObj);
    }

    @Override
    public void updateCouponTemplateStatus(Long id, Integer status) {
        // 校验存在
        validateCouponTemplateExists(id);
        // 更新
        couponTemplateMapper.updateById(new SalesCouponTemplateDO().setId(id).setStatus(status));
    }

    @Override
    public void deleteCouponTemplate(Long id) {
        // 校验存在
        validateCouponTemplateExists(id);
        // 删除
        couponTemplateMapper.deleteById(id);
    }

    private SalesCouponTemplateDO validateCouponTemplateExists(Long id) {
        SalesCouponTemplateDO couponTemplate = couponTemplateMapper.selectById(id);
        if (couponTemplate == null) {
            throw exception(COUPON_TEMPLATE_NOT_EXISTS);
        }
        return couponTemplate;
    }

    private void validateProductScope(Integer productScope, List<Long> productScopeValues) {
        if (Objects.equals(SalesProductScopeEnum.SPU.getScope(), productScope)) {
            productSpuApi.validateSpuList(productScopeValues);
        } else if (Objects.equals(SalesProductScopeEnum.CATEGORY.getScope(), productScope)) {
            productCategoryApi.validateCategoryList(productScopeValues);
        }
    }

    @Override
    public SalesCouponTemplateDO getCouponTemplate(Long id) {
        return couponTemplateMapper.selectById(id);
    }

    @Override
    public PageResult<SalesCouponTemplateDO> getCouponTemplatePage(SalesCouponTemplatePageReqVO pageReqVO) {
        return couponTemplateMapper.selectPage(pageReqVO);
    }

    @Override
    public void updateCouponTemplateTakeCount(Long id, int incrCount) {
        int updateCount = couponTemplateMapper.updateTakeCount(id, incrCount);
        if (updateCount == 0) {
            throw exception(COUPON_TEMPLATE_NOT_ENOUGH);
        }
    }

    @Override
    public List<SalesCouponTemplateDO> getCouponTemplateListByTakeType(SalesCouponTakeTypeEnum takeType) {
        return couponTemplateMapper.selectListByTakeType(takeType.getType());
    }

    @Override
    public List<SalesCouponTemplateDO> getCouponTemplateList(List<Integer> canTakeTypes, Integer productScope,
                                                        Long productScopeValue, Integer count) {
        return couponTemplateMapper.selectList(canTakeTypes, productScope, productScopeValue, count);
    }

    @Override
    public List<SalesCouponTemplateDO> getCouponTemplateList(Collection<Long> ids) {
        return couponTemplateMapper.selectByIds(ids);
    }

}
