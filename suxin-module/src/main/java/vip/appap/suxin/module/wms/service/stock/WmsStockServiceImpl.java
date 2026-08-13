package vip.appap.suxin.module.wms.service.stock;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import java.math.BigDecimal;
import org.springframework.validation.annotation.Validated;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.system.util.BaseService;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import vip.appap.suxin.module.wms.controller.admin.stock.vo.*;
import vip.appap.suxin.module.wms.dal.dataobject.stock.WmsStockDO;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.pojo.PageParam;
import vip.appap.suxin.framework.common.util.object.BeanUtils;

import vip.appap.suxin.module.wms.dal.mysql.stock.WmsStockMapper;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertList;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.diffList;
import static vip.appap.suxin.module.wms.enums.ErrorCodeConstants.*;

/**
 * 库存快照 Service 实现类
 *
 * @author admin
 */
@Service
@Validated
public class WmsStockServiceImpl extends BaseService implements WmsStockService {

    @Resource
    private WmsStockMapper stockMapper;

    @Override
    public Long createStock(WmsStockSaveReqVO createReqVO) {
        // 插入
        WmsStockDO stock = BeanUtils.toBean(createReqVO, WmsStockDO.class);
        stockMapper.insert(stock);

        // 返回
        return stock.getId();
    }

    @Override
    public void updateStock(WmsStockSaveReqVO updateReqVO) {
        // 校验存在
        validateStockExists(updateReqVO.getId());
        // 更新
        WmsStockDO updateObj = BeanUtils.toBean(updateReqVO, WmsStockDO.class);
        stockMapper.updateById(updateObj);
    }

    @Override
    public void deleteStock(Long id) {
        // 校验存在
        validateStockExists(id);
        // 删除
        stockMapper.deleteById(id);
    }

    @Override
        public void deleteStockListByIds(List<Long> ids) {
        // 删除
        stockMapper.deleteByIds(ids);
        }


    private void validateStockExists(Long id) {
        if (stockMapper.selectById(id) == null) {
            throw exception(STOCK_NOT_EXISTS);
        }
    }

    @Override
    public WmsStockDO getStock(Long id) {
        return stockMapper.selectById(id);
    }

    @Override
    public PageResult<WmsStockDO> getStockPage(WmsStockPageReqVO pageReqVO) {
        return stockMapper.selectPage(pageReqVO);
    }

    @Override
    public int cleanZeroStock() {
        // 仅清理库存数量与预占数量均为 0 的记录，存在预占的不予删除
        return stockMapper.delete(new LambdaQueryWrapperX<WmsStockDO>()
                .eq(WmsStockDO::getQuantity, BigDecimal.ZERO)
                .eq(WmsStockDO::getReservedQuantity, BigDecimal.ZERO));
    }

}