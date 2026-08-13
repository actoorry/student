package vip.appap.suxin.module.wms.service.ordertype;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import vip.appap.suxin.module.system.util.BaseService;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import vip.appap.suxin.module.wms.controller.admin.ordertype.vo.*;
import vip.appap.suxin.module.wms.dal.dataobject.ordertype.WmsOrderTypeDO;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.pojo.PageParam;
import vip.appap.suxin.framework.common.util.object.BeanUtils;

import vip.appap.suxin.module.wms.dal.mysql.ordertype.WmsOrderTypeMapper;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertList;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.diffList;
import static vip.appap.suxin.module.wms.enums.ErrorCodeConstants.*;

/**
 * 出入库单据类型配置 Service 实现类
 *
 * @author admin
 */
@Service
@Validated
public class WmsOrderTypeServiceImpl extends BaseService implements WmsOrderTypeService {

    @Resource
    private WmsOrderTypeMapper orderTypeMapper;

    @Override
    public Long createOrderType(WmsOrderTypeSaveReqVO createReqVO) {
        // 插入
        WmsOrderTypeDO orderType = BeanUtils.toBean(createReqVO, WmsOrderTypeDO.class);
        orderTypeMapper.insert(orderType);

        // 返回
        return orderType.getId();
    }

    @Override
    public void updateOrderType(WmsOrderTypeSaveReqVO updateReqVO) {
        // 校验存在
        validateOrderTypeExists(updateReqVO.getId());
        // 更新
        WmsOrderTypeDO updateObj = BeanUtils.toBean(updateReqVO, WmsOrderTypeDO.class);
        orderTypeMapper.updateById(updateObj);
    }

    @Override
    public void deleteOrderType(Long id) {
        // 校验存在
        validateOrderTypeExists(id);
        // 删除
        orderTypeMapper.deleteById(id);
    }

    @Override
        public void deleteOrderTypeListByIds(List<Long> ids) {
        // 删除
        orderTypeMapper.deleteByIds(ids);
        }


    private void validateOrderTypeExists(Long id) {
        if (orderTypeMapper.selectById(id) == null) {
            throw exception(ORDER_TYPE_NOT_EXISTS);
        }
    }

    @Override
    public WmsOrderTypeDO getOrderType(Long id) {
        return orderTypeMapper.selectById(id);
    }

    @Override
    public PageResult<WmsOrderTypeDO> getOrderTypePage(WmsOrderTypePageReqVO pageReqVO) {
        return orderTypeMapper.selectPage(pageReqVO);
    }

}