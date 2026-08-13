package vip.appap.suxin.module.sales.service;

import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.tenant.core.context.TenantContextHolder;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDeliveryExpressCreateReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDeliveryExpressExportReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDeliveryExpressPageReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDeliveryExpressUpdateReqVO;
import vip.appap.suxin.module.sales.convert.SalesDeliveryExpressConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDeliveryExpressDO;
import vip.appap.suxin.module.sales.dal.mysql.SalesDeliveryExpressMapper;
import vip.appap.suxin.module.sales.dal.mysql.SalesDeliveryReferenceMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.List;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.module.sales.enums.ErrorCodeConstants.*;

/**
 * 快递公司 Service 实现类
 *
 * @author jason
 */
@Service
@Validated
public class SalesDeliveryExpressServiceImpl implements SalesDeliveryExpressService {

    @Resource
    private SalesDeliveryExpressMapper deliveryExpressMapper;
    @Resource
    private SalesDeliveryReferenceMapper deliveryReferenceMapper;

    @Override
    public Long createDeliveryExpress(SalesDeliveryExpressCreateReqVO createReqVO) {
        //校验编码是否唯一
        validateExpressCodeUnique(createReqVO.getCode(), null);
        // 插入
        SalesDeliveryExpressDO deliveryExpress = SalesDeliveryExpressConvert.INSTANCE.convert(createReqVO);
        deliveryExpressMapper.insert(deliveryExpress);
        // 返回
        return deliveryExpress.getId();
    }

    @Override
    public void updateDeliveryExpress(SalesDeliveryExpressUpdateReqVO updateReqVO) {
        // 校验存在
        validateDeliveryExpressExists(updateReqVO.getId());
        //校验编码是否唯一
        validateExpressCodeUnique(updateReqVO.getCode(), updateReqVO.getId());
        // 更新
        SalesDeliveryExpressDO updateObj = SalesDeliveryExpressConvert.INSTANCE.convert(updateReqVO);
        deliveryExpressMapper.updateById(updateObj);
    }

    @Override
    public void deleteDeliveryExpress(Long id) {
        // 校验存在
        validateDeliveryExpressExists(id);
        // 校验历史引用：订单或售后仍引用时拒绝删除，引导改为停用
        Long tenantId = TenantContextHolder.getTenantId();
        if (deliveryReferenceMapper.selectCountOrderByLogisticsId(id, tenantId) > 0
                || deliveryReferenceMapper.selectCountAfterSaleByLogisticsId(id, tenantId) > 0) {
            throw exception(EXPRESS_REFERENCED);
        }
        // 删除
        deliveryExpressMapper.deleteById(id);
    }

    private void validateExpressCodeUnique(String code, Long id) {
        SalesDeliveryExpressDO express = deliveryExpressMapper.selectByCode(code);
        if (express == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的快递公司
        if (id == null) {
            throw exception(EXPRESS_CODE_DUPLICATE);
        }
        if (!express.getId().equals(id)) {
            throw exception(EXPRESS_CODE_DUPLICATE);
        }
    }
    private void validateDeliveryExpressExists(Long id) {
        if (deliveryExpressMapper.selectById(id) == null) {
            throw exception(EXPRESS_NOT_EXISTS);
        }
    }

    @Override
    public SalesDeliveryExpressDO getDeliveryExpress(Long id) {
        return deliveryExpressMapper.selectById(id);
    }

    @Override
    public SalesDeliveryExpressDO validateDeliveryExpress(Long id) {
        SalesDeliveryExpressDO deliveryExpress = deliveryExpressMapper.selectById(id);
        if (deliveryExpress == null) {
            throw exception(EXPRESS_NOT_EXISTS);
        }
        if (deliveryExpress.getStatus().equals(CommonStatusEnum.DISABLE.getStatus())) {
            throw exception(EXPRESS_STATUS_NOT_ENABLE);
        }
        return deliveryExpress;
    }

    @Override
    public PageResult<SalesDeliveryExpressDO> getDeliveryExpressPage(SalesDeliveryExpressPageReqVO pageReqVO) {
        return deliveryExpressMapper.selectPage(pageReqVO);
    }

    @Override
    public List<SalesDeliveryExpressDO> getDeliveryExpressList(SalesDeliveryExpressExportReqVO exportReqVO) {
        return deliveryExpressMapper.selectList(exportReqVO);
    }

    @Override
    public List<SalesDeliveryExpressDO> getDeliveryExpressListByStatus(Integer status) {
        return deliveryExpressMapper.selectListByStatus(status);
    }

}
