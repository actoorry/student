package vip.appap.suxin.module.wms.service.orderitem;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import vip.appap.suxin.module.system.util.BaseService;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import vip.appap.suxin.module.wms.controller.admin.orderitem.vo.*;
import vip.appap.suxin.module.wms.dal.dataobject.orderitem.WmsOrderItemDO;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.pojo.PageParam;
import vip.appap.suxin.framework.common.util.object.BeanUtils;

import vip.appap.suxin.module.wms.dal.mysql.orderitem.WmsOrderItemMapper;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertList;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.diffList;
import static vip.appap.suxin.module.wms.enums.ErrorCodeConstants.*;

/**
 * 出入库/调拨单据明细 Service 实现类
 *
 * @author admin
 */
@Service
@Validated
public class WmsOrderItemServiceImpl extends BaseService implements WmsOrderItemService {

    @Resource
    private WmsOrderItemMapper orderItemMapper;

    @Override
    public Long createOrderItem(WmsOrderItemSaveReqVO createReqVO) {
        // 插入
        WmsOrderItemDO orderItem = BeanUtils.toBean(createReqVO, WmsOrderItemDO.class);
        orderItemMapper.insert(orderItem);

        // 返回
        return orderItem.getId();
    }

    @Override
    public void updateOrderItem(WmsOrderItemSaveReqVO updateReqVO) {
        // 校验存在
        validateOrderItemExists(updateReqVO.getId());
        // 更新
        WmsOrderItemDO updateObj = BeanUtils.toBean(updateReqVO, WmsOrderItemDO.class);
        orderItemMapper.updateById(updateObj);
    }

    @Override
    public void deleteOrderItem(Long id) {
        // 校验存在
        validateOrderItemExists(id);
        // 删除
        orderItemMapper.deleteById(id);
    }

    @Override
        public void deleteOrderItemListByIds(List<Long> ids) {
        // 删除
        orderItemMapper.deleteByIds(ids);
        }


    private void validateOrderItemExists(Long id) {
        if (orderItemMapper.selectById(id) == null) {
            throw exception(ORDER_ITEM_NOT_EXISTS);
        }
    }

    @Override
    public WmsOrderItemDO getOrderItem(Long id) {
        return orderItemMapper.selectById(id);
    }

    @Override
    public PageResult<WmsOrderItemDO> getOrderItemPage(WmsOrderItemPageReqVO pageReqVO) {
        return orderItemMapper.selectPage(pageReqVO);
    }

}