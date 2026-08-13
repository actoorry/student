package vip.appap.suxin.module.sales.service;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesBannerCreateReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesBannerPageReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesBannerUpdateReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesBannerDO;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 首页 Banner Service 接口
 *
 * @author xia
 */
public interface SalesBannerService {

    /**
     * 创建 Banner
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createBanner(@Valid SalesBannerCreateReqVO createReqVO);

    /**
     * 更新 Banner
     *
     * @param updateReqVO 更新信息
     */
    void updateBanner(@Valid SalesBannerUpdateReqVO updateReqVO);

    /**
     * 删除 Banner
     *
     * @param id 编号
     */
    void deleteBanner(Long id);

    /**
     * 获得 Banner
     *
     * @param id 编号
     * @return Banner
     */
    SalesBannerDO getBanner(Long id);

    /**
     * 获得 Banner 分页
     *
     * @param pageReqVO 分页查询
     * @return Banner分页
     */
    PageResult<SalesBannerDO> getBannerPage(SalesBannerPageReqVO pageReqVO);

    /**
     * 增加 Banner 点击量
     *
     * @param id Banner编号
     */
    void addBannerBrowseCount(Long id);

    /**
     * 获得 Banner 列表
     *
     * @param position 定位
     * @return Banner 列表
     */
    List<SalesBannerDO> getBannerListByPosition(Integer position);

}
