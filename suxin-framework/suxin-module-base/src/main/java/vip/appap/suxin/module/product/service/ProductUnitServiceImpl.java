package vip.appap.suxin.module.product.service;

import cn.hutool.core.util.StrUtil;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.product.controller.admin.vo.ProductUnitCreateReqVO;
import vip.appap.suxin.module.product.controller.admin.vo.ProductUnitPageReqVO;
import vip.appap.suxin.module.product.controller.admin.vo.ProductUnitUpdateReqVO;
import vip.appap.suxin.module.product.convert.ProductUnitConvert;
import vip.appap.suxin.module.product.dal.dataobject.ProductUnitDO;
import vip.appap.suxin.module.product.dal.mysql.ProductUnitMapper;
import vip.appap.suxin.module.product.enums.DictTypeConstants;
import vip.appap.suxin.module.system.api.DictDataApi;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.module.product.enums.ErrorCodeConstants.*;

/**
 * 产品单位 Service 实现类
 */
@Service
@Validated
public class ProductUnitServiceImpl implements ProductUnitService {

    @Resource
    private ProductUnitMapper unitMapper;
    @Resource
    private DictDataApi dictDataApi;

    @Override
    public Long createUnit(ProductUnitCreateReqVO createReqVO) {
        validateUnitNameUnique(null, createReqVO.getName());
        validateUnitType(createReqVO.getType());
        validateUnitConversion(createReqVO);

        ProductUnitDO unit = ProductUnitConvert.INSTANCE.convert(createReqVO);
        unitMapper.insert(unit);
        return unit.getId();
    }

    @Override
    public void updateUnit(ProductUnitUpdateReqVO updateReqVO) {
        ProductUnitDO oldUnit = validateUnitExists(updateReqVO.getId());
        validateBaseUnitEditable(oldUnit);
        validateUnitNameUnique(updateReqVO.getId(), updateReqVO.getName());
        validateUnitType(updateReqVO.getType());
        validateUnitConversion(updateReqVO);

        ProductUnitDO updateObj = ProductUnitConvert.INSTANCE.convert(updateReqVO);
        unitMapper.updateById(updateObj);
    }

    @Override
    public void deleteUnit(Long id) {
        ProductUnitDO unit = validateUnitExists(id);
        validateBaseUnitEditable(unit);
        unitMapper.deleteById(id);
    }

    private ProductUnitDO validateUnitExists(Long id) {
        ProductUnitDO unit = unitMapper.selectById(id);
        if (unit == null) {
            throw exception(UNIT_NOT_EXISTS);
        }
        return unit;
    }

    @VisibleForTesting
    public void validateUnitNameUnique(Long id, String name) {
        ProductUnitDO unit = unitMapper.selectByName(name);
        if (unit == null) {
            return;
        }
        if (id == null || !unit.getId().equals(id)) {
            throw exception(UNIT_NAME_EXISTS);
        }
    }

    @Override
    public ProductUnitDO getUnit(Long id) {
        return unitMapper.selectById(id);
    }

    @Override
    public List<ProductUnitDO> getUnitList(Collection<Long> ids) {
        return unitMapper.selectByIds(ids);
    }

    @Override
    public PageResult<ProductUnitDO> getUnitPage(ProductUnitPageReqVO pageReqVO) {
        return unitMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ProductUnitDO> getUnitListByStatus(Integer status) {
        return unitMapper.selectListByStatus(status);
    }

    private void validateUnitType(Integer type) {
        if (Integer.valueOf(0).equals(type)) {
            throw exception(UNIT_BASE_UNIT_NOT_EDITABLE);
        }
        dictDataApi.validateDictDataList(DictTypeConstants.PRODUCT_UNIT_TYPE, Collections.singleton(String.valueOf(type)));
    }

    private void validateUnitConversion(ProductUnitCreateReqVO reqVO) {
        validateUnitConversion(reqVO.getRelativeFactor());
    }

    private void validateUnitConversion(ProductUnitUpdateReqVO reqVO) {
        validateUnitConversion(reqVO.getRelativeFactor());
    }

    private void validateUnitConversion(String relativeFactor) {
        BigDecimal factor = parsePositiveNumber(relativeFactor);
        if (factor == null) {
            throw exception(UNIT_RELATIVE_FACTOR_INVALID);
        }
    }

    private BigDecimal parsePositiveNumber(String value) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        try {
            BigDecimal number = new BigDecimal(value);
            return number.compareTo(BigDecimal.ZERO) > 0 ? number : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void validateBaseUnitEditable(ProductUnitDO unit) {
        if (unit != null && isBaseUnit(unit)) {
            throw exception(UNIT_BASE_UNIT_NOT_EDITABLE);
        }
    }

    private boolean isBaseUnit(ProductUnitDO unit) {
        return Integer.valueOf(0).equals(unit.getType()) || StrUtil.isBlank(unit.getRelativeFactor());
    }

}
