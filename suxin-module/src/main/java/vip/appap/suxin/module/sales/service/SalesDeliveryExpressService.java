package vip.appap.suxin.module.sales.service;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDeliveryExpressCreateReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDeliveryExpressExportReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDeliveryExpressPageReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDeliveryExpressUpdateReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDeliveryExpressDO;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 快递公司 Service 接口
 *
 * @author jason
 */
public interface SalesDeliveryExpressService {

    /**
     * 创建快递公司
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createDeliveryExpress(@Valid SalesDeliveryExpressCreateReqVO createReqVO);

    /**
     * 更新快递公司
     *
     * @param updateReqVO 更新信息
     */
    void updateDeliveryExpress(@Valid SalesDeliveryExpressUpdateReqVO updateReqVO);

    /**
     * 删除快递公司
     *
     * @param id 编号
     */
    void deleteDeliveryExpress(Long id);

    /**
     * 获得快递公司
     *
     * @param id 编号
     * @return 快递公司
     */
    SalesDeliveryExpressDO getDeliveryExpress(Long id);

    /**
     * 校验快递公司是否合法
     *
     * @param id 编号
     * @return 快递公司
     */
    SalesDeliveryExpressDO validateDeliveryExpress(Long id);

    /**
     * 获得快递公司分页
     *
     * @param pageReqVO 分页查询
     * @return 快递公司分页
     */
    PageResult<SalesDeliveryExpressDO> getDeliveryExpressPage(SalesDeliveryExpressPageReqVO pageReqVO);

    /**
     * 获得快递公司列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 快递公司列表
     */
    List<SalesDeliveryExpressDO> getDeliveryExpressList(SalesDeliveryExpressExportReqVO exportReqVO);

    /**
     * 获取指定状态的快递公司列表
     *
     * @param status 状态
     * @return 快递公司列表
     */
    List<SalesDeliveryExpressDO> getDeliveryExpressListByStatus(Integer status);

}
