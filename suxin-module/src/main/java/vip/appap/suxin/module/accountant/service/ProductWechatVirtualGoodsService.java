package vip.appap.suxin.module.accountant.service;

import vip.appap.suxin.module.product.controller.admin.vo.ProductWechatVirtualGoodsRefreshReqVO;
import vip.appap.suxin.module.product.controller.admin.vo.ProductWechatVirtualGoodsRespVO;
import vip.appap.suxin.module.product.controller.admin.vo.ProductWechatVirtualGoodsSyncReqVO;

import java.util.List;

public interface ProductWechatVirtualGoodsService {

    /**
     * 查询微信虚拟支付道具状态
     */
    List<ProductWechatVirtualGoodsRespVO> getWechatVirtualGoodsList(Long spuId);

    /**
     * 同步微信虚拟支付道具（上传）
     */
    List<ProductWechatVirtualGoodsRespVO> syncWechatVirtualGoods(ProductWechatVirtualGoodsSyncReqVO reqVO);

    /**
     * 发布微信虚拟支付道具
     */
    List<ProductWechatVirtualGoodsRespVO> publishWechatVirtualGoods(ProductWechatVirtualGoodsSyncReqVO reqVO);

    /**
     * 刷新微信虚拟支付道具状态
     */
    List<ProductWechatVirtualGoodsRespVO> refreshWechatVirtualGoods(ProductWechatVirtualGoodsRefreshReqVO reqVO);

}
