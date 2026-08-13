package vip.appap.suxin.module.sales.service;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.system.api.AdminUserApi;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDeliveryPickUpBindReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDeliveryPickUpStoreCreateReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDeliveryPickUpStorePageReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDeliveryPickUpStoreUpdateReqVO;
import vip.appap.suxin.module.sales.convert.SalesDeliveryPickUpStoreConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDeliveryPickUpStoreDO;
import vip.appap.suxin.module.sales.dal.mysql.SalesDeliveryPickUpStoreMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.module.sales.enums.ErrorCodeConstants.PICK_UP_STORE_NOT_EXISTS;

/**
 * 自提门店 Service 实现类
 *
 * @author jason
 */
@Service
@Validated
public class SalesDeliveryPickUpStoreServiceImpl implements SalesDeliveryPickUpStoreService {

    @Resource
    private SalesDeliveryPickUpStoreMapper deliveryPickUpStoreMapper;

    @Resource
    private AdminUserApi adminUserApi;

    @Override
    public Long createDeliveryPickUpStore(SalesDeliveryPickUpStoreCreateReqVO createReqVO) {
        // 插入
        SalesDeliveryPickUpStoreDO deliveryPickUpStore = SalesDeliveryPickUpStoreConvert.INSTANCE.convert(createReqVO);
        deliveryPickUpStoreMapper.insert(deliveryPickUpStore);
        // 返回
        return deliveryPickUpStore.getId();
    }

    @Override
    public void updateDeliveryPickUpStore(SalesDeliveryPickUpStoreUpdateReqVO updateReqVO) {
        // 校验存在
        validateDeliveryPickUpStoreExists(updateReqVO.getId());
        // 更新
        SalesDeliveryPickUpStoreDO updateObj = SalesDeliveryPickUpStoreConvert.INSTANCE.convert(updateReqVO);
        deliveryPickUpStoreMapper.updateById(updateObj);
    }

    @Override
    public void deleteDeliveryPickUpStore(Long id) {
        // 校验存在
        validateDeliveryPickUpStoreExists(id);
        // 删除
        deliveryPickUpStoreMapper.deleteById(id);
    }

    private void validateDeliveryPickUpStoreExists(Long id) {
        SalesDeliveryPickUpStoreDO deliveryPickUpStore = deliveryPickUpStoreMapper.selectById(id);
        if (deliveryPickUpStore == null) {
            throw exception(PICK_UP_STORE_NOT_EXISTS);
        }
    }

    @Override
    public SalesDeliveryPickUpStoreDO getDeliveryPickUpStore(Long id) {
        return deliveryPickUpStoreMapper.selectById(id);
    }

    @Override
    public List<SalesDeliveryPickUpStoreDO> getDeliveryPickUpStoreList(Collection<Long> ids) {
        return deliveryPickUpStoreMapper.selectByIds(ids);
    }

    @Override
    public List<SalesDeliveryPickUpStoreDO> getDeliveryPickUpStoreListByStatus(Integer status) {
        return deliveryPickUpStoreMapper.selectListByStatus(status);
    }

    @Override
    public PageResult<SalesDeliveryPickUpStoreDO> getDeliveryPickUpStorePage(SalesDeliveryPickUpStorePageReqVO pageReqVO) {
        return deliveryPickUpStoreMapper.selectPage(pageReqVO);
    }

    @Override
    public void bindDeliveryPickUpStore(SalesDeliveryPickUpBindReqVO bindReqVO) {
        // 1.1 校验门店存在
        validateDeliveryPickUpStoreExists(bindReqVO.getId());
        // 1.2 校验用户存在
        adminUserApi.validateUserList(bindReqVO.getVerifyUserIds());

        // 2. 更新
        SalesDeliveryPickUpStoreDO updateObj = BeanUtils.toBean(bindReqVO, SalesDeliveryPickUpStoreDO.class);
        deliveryPickUpStoreMapper.updateById(updateObj);
    }

}
