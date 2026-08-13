package vip.appap.suxin.module.wms.service.warehouse;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import vip.appap.suxin.module.system.util.BaseService;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import vip.appap.suxin.module.wms.controller.admin.warehouse.vo.*;
import vip.appap.suxin.module.wms.dal.dataobject.warehouse.WmsWarehouseDO;
import vip.appap.suxin.module.wms.dal.dataobject.stock.WmsStockDO;
import vip.appap.suxin.module.wms.dal.dataobject.order.WmsOrderDO;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.pojo.PageParam;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.framework.ip.core.utils.AreaUtils;
import vip.appap.suxin.module.partner.api.PartnerApi;
import vip.appap.suxin.module.partner.api.dto.PartnerRespDTO;

import vip.appap.suxin.module.wms.dal.mysql.warehouse.WmsWarehouseMapper;
import vip.appap.suxin.module.wms.dal.mysql.stock.WmsStockMapper;
import vip.appap.suxin.module.wms.dal.mysql.order.WmsOrderMapper;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertList;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.diffList;
import static vip.appap.suxin.module.wms.enums.ErrorCodeConstants.*;

/**
 * 仓库位置表（仓库/库区/库位树形结构） Service 实现类
 *
 * @author admin
 */
@Service
@Validated
public class WmsWarehouseServiceImpl extends BaseService implements WmsWarehouseService {

    @Resource
    private WmsWarehouseMapper warehouseMapper;
    @Resource
    private WmsStockMapper stockMapper;
    @Resource
    private WmsOrderMapper orderMapper;
    @Resource
    private PartnerApi partnerApi;

    @Override
    public Long createWarehouse(WmsWarehouseSaveReqVO createReqVO) {
        // 校验 areaId 在 Area 表中真实存在
        validateAreaId(createReqVO.getAreaId());
        // 校验 partnerId：仅允许公司类型客商，仅一级实体仓库可配置
        validatePartnerId(createReqVO.getPartnerId(), createReqVO.getParentId());
        // 插入
        WmsWarehouseDO warehouse = BeanUtils.toBean(createReqVO, WmsWarehouseDO.class);
        warehouseMapper.insert(warehouse);

        // 返回
        return warehouse.getId();
    }

    @Override
    public void updateWarehouse(WmsWarehouseSaveReqVO updateReqVO) {
        // 校验存在
        validateWarehouseExists(updateReqVO.getId());
        // 校验 areaId 在 Area 表中真实存在
        validateAreaId(updateReqVO.getAreaId());
        // 校验 partnerId：仅允许公司类型客商，仅一级实体仓库可配置
        validatePartnerId(updateReqVO.getPartnerId(), updateReqVO.getParentId());
        // 更新
        WmsWarehouseDO updateObj = BeanUtils.toBean(updateReqVO, WmsWarehouseDO.class);
        warehouseMapper.updateById(updateObj);
    }

    @Override
    public void deleteWarehouse(Long id) {
        // 校验存在
        validateWarehouseExists(id);
        // 校验无子节点
        validateNoChildren(id);
        // 校验无库存引用
        validateNoStockRef(id);
        // 校验无单据引用
        validateNoOrderRef(id);
        // 删除
        warehouseMapper.deleteById(id);
    }

    @Override
    public void deleteWarehouseListByIds(List<Long> ids) {
        for (Long id : ids) {
            deleteWarehouse(id);
        }
    }

    // ========== 私有校验方法 ==========

    private void validateWarehouseExists(Long id) {
        if (warehouseMapper.selectById(id) == null) {
            throw exception(WAREHOUSE_NOT_EXISTS);
        }
    }

    /** 校验 areaId 在 Area 表中真实存在 */
    private void validateAreaId(Integer areaId) {
        if (areaId != null && AreaUtils.getArea(areaId) == null) {
            throw exception(WAREHOUSE_AREA_NOT_EXISTS);
        }
    }

    /** 校验 partnerId：仅允许公司类型客商，仅一级实体仓库可配置 */
    private void validatePartnerId(Long partnerId, Long parentId) {
        if (partnerId == null) return;
        // 仅一级实体仓库（parent_id=0）可配置负责人
        if (parentId != null && parentId != 0) {
            throw exception(WAREHOUSE_PARTNER_NOT_ALLOW);
        }
        // 校验客商存在且为公司类型
        PartnerRespDTO partner = partnerApi.getPartner(partnerId);
        if (partner == null) {
            throw exception(WAREHOUSE_PARTNER_NOT_COMPANY);
        }
        if (!Boolean.TRUE.equals(partner.getIsCompany())) {
            throw exception(WAREHOUSE_PARTNER_NOT_COMPANY);
        }
    }

    /** 校验无子节点 */
    private void validateNoChildren(Long id) {
        Long childCount = warehouseMapper.selectCount(WmsWarehouseDO::getParentId, id);
        if (childCount > 0) {
            throw exception(WAREHOUSE_EXISTS_CHILDREN);
        }
    }

    /** 校验无库存引用 */
    private void validateNoStockRef(Long id) {
        Long stockCount = stockMapper.selectCount(WmsStockDO::getWarehouseId, id);
        if (stockCount > 0) {
            throw exception(WAREHOUSE_HAS_STOCK);
        }
        stockCount = stockMapper.selectCount(WmsStockDO::getLocationId, id);
        if (stockCount > 0) {
            throw exception(WAREHOUSE_HAS_STOCK);
        }
    }

    /** 校验无单据引用 */
    private void validateNoOrderRef(Long id) {
        Long orderCount = orderMapper.selectCount(WmsOrderDO::getFromWarehouseId, id);
        if (orderCount > 0) {
            throw exception(WAREHOUSE_HAS_ORDER);
        }
        orderCount = orderMapper.selectCount(WmsOrderDO::getToWarehouseId, id);
        if (orderCount > 0) {
            throw exception(WAREHOUSE_HAS_ORDER);
        }
    }

    @Override
    public WmsWarehouseDO getWarehouse(Long id) {
        return warehouseMapper.selectById(id);
    }

    @Override
    public PageResult<WmsWarehouseDO> getWarehousePage(WmsWarehousePageReqVO pageReqVO) {
        return warehouseMapper.selectPage(pageReqVO);
    }

}