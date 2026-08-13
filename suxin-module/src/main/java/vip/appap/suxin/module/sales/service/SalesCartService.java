package vip.appap.suxin.module.sales.service;

import jakarta.validation.Valid;
import vip.appap.suxin.module.partner.dal.dataobject.SalesCartDO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesCartAddReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesCartListRespVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesCartResetReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesCartUpdateCountReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesCartUpdateSelectedReqVO;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 销售购物车 Service 接口。
 */
public interface SalesCartService {

    Long addCart(Long userId, @Valid AppSalesCartAddReqVO addReqVO);

    void updateCartCount(Long userId, @Valid AppSalesCartUpdateCountReqVO updateReqVO);

    void updateCartSelected(Long userId, @Valid AppSalesCartUpdateSelectedReqVO updateReqVO);

    void resetCart(Long userId, @Valid AppSalesCartResetReqVO resetReqVO);

    void deleteCart(Long userId, Collection<Long> ids);

    Integer getCartCount(Long userId);

    AppSalesCartListRespVO getCartList(Long userId);

    List<SalesCartDO> getCartList(Long userId, Set<Long> ids);

}
