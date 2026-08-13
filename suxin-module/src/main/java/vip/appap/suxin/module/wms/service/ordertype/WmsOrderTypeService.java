package vip.appap.suxin.module.wms.service.ordertype;

import java.util.*;
import jakarta.validation.*;
import vip.appap.suxin.module.wms.controller.admin.ordertype.vo.*;
import vip.appap.suxin.module.wms.dal.dataobject.ordertype.WmsOrderTypeDO;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.pojo.PageParam;

/**
 * 出入库单据类型配置 Service 接口
 *
 * @author admin
 */
public interface WmsOrderTypeService {

    /**
     * 创建出入库单据类型配置
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createOrderType(@Valid WmsOrderTypeSaveReqVO createReqVO);

    /**
     * 更新出入库单据类型配置
     *
     * @param updateReqVO 更新信息
     */
    void updateOrderType(@Valid WmsOrderTypeSaveReqVO updateReqVO);

    /**
     * 删除出入库单据类型配置
     *
     * @param id 编号
     */
    void deleteOrderType(Long id);

    /**
    * 批量删除出入库单据类型配置
    *
    * @param ids 编号
    */
    void deleteOrderTypeListByIds(List<Long> ids);

    /**
     * 获得出入库单据类型配置
     *
     * @param id 编号
     * @return 出入库单据类型配置
     */
    WmsOrderTypeDO getOrderType(Long id);

    /**
     * 获得出入库单据类型配置分页
     *
     * @param pageReqVO 分页查询
     * @return 出入库单据类型配置分页
     */
    PageResult<WmsOrderTypeDO> getOrderTypePage(WmsOrderTypePageReqVO pageReqVO);

}