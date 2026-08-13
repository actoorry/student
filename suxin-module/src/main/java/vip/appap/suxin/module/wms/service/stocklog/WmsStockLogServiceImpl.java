package vip.appap.suxin.module.wms.service.stocklog;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import vip.appap.suxin.module.system.util.BaseService;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import vip.appap.suxin.module.wms.controller.admin.stocklog.vo.*;
import vip.appap.suxin.module.wms.dal.dataobject.stocklog.WmsStockLogDO;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.pojo.PageParam;
import vip.appap.suxin.framework.common.util.object.BeanUtils;

import vip.appap.suxin.module.wms.dal.mysql.stocklog.WmsStockLogMapper;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertList;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.diffList;
import static vip.appap.suxin.module.wms.enums.ErrorCodeConstants.*;

/**
 * 库存流水台账 Service 实现类
 *
 * @author admin
 */
@Service
@Validated
public class WmsStockLogServiceImpl extends BaseService implements WmsStockLogService {

    @Resource
    private WmsStockLogMapper stockLogMapper;

    @Override
    public Long createStockLog(WmsStockLogSaveReqVO createReqVO) {
        // 插入
        WmsStockLogDO stockLog = BeanUtils.toBean(createReqVO, WmsStockLogDO.class);
        stockLogMapper.insert(stockLog);

        // 返回
        return stockLog.getId();
    }

    @Override
    public void updateStockLog(WmsStockLogSaveReqVO updateReqVO) {
        // 校验存在
        validateStockLogExists(updateReqVO.getId());
        // 更新
        WmsStockLogDO updateObj = BeanUtils.toBean(updateReqVO, WmsStockLogDO.class);
        stockLogMapper.updateById(updateObj);
    }

    @Override
    public void deleteStockLog(Long id) {
        // 校验存在
        validateStockLogExists(id);
        // 删除
        stockLogMapper.deleteById(id);
    }

    @Override
        public void deleteStockLogListByIds(List<Long> ids) {
        // 删除
        stockLogMapper.deleteByIds(ids);
        }


    private void validateStockLogExists(Long id) {
        if (stockLogMapper.selectById(id) == null) {
            throw exception(STOCK_LOG_NOT_EXISTS);
        }
    }

    @Override
    public WmsStockLogDO getStockLog(Long id) {
        return stockLogMapper.selectById(id);
    }

    @Override
    public PageResult<WmsStockLogDO> getStockLogPage(WmsStockLogPageReqVO pageReqVO) {
        return stockLogMapper.selectPage(pageReqVO);
    }

}