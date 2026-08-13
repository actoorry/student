package vip.appap.suxin.module.sales.service;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesBannerCreateReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesBannerPageReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesBannerUpdateReqVO;
import vip.appap.suxin.module.sales.convert.SalesBannerConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesBannerDO;
import vip.appap.suxin.module.sales.dal.mysql.SalesBannerMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.List;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.module.sales.enums.SalesErrorCodeConstants.BANNER_NOT_EXISTS;

/**
 * 首页 banner 实现类
 *
 * @author xia
 */
@Service
@Validated
public class SalesBannerServiceImpl implements SalesBannerService {

    @Resource
    private SalesBannerMapper bannerMapper;

    @Override
    public Long createBanner(SalesBannerCreateReqVO createReqVO) {
        // 插入
        SalesBannerDO banner = SalesBannerConvert.INSTANCE.convert(createReqVO);
        bannerMapper.insert(banner);
        // 返回
        return banner.getId();
    }

    @Override
    public void updateBanner(SalesBannerUpdateReqVO updateReqVO) {
        // 校验存在
        this.validateBannerExists(updateReqVO.getId());
        // 更新
        SalesBannerDO updateObj = SalesBannerConvert.INSTANCE.convert(updateReqVO);
        bannerMapper.updateById(updateObj);
    }

    @Override
    public void deleteBanner(Long id) {
        // 校验存在
        this.validateBannerExists(id);
        // 删除
        bannerMapper.deleteById(id);
    }

    private void validateBannerExists(Long id) {
        if (bannerMapper.selectById(id) == null) {
            throw exception(BANNER_NOT_EXISTS);
        }
    }

    @Override
    public SalesBannerDO getBanner(Long id) {
        return bannerMapper.selectById(id);
    }

    @Override
    public PageResult<SalesBannerDO> getBannerPage(SalesBannerPageReqVO pageReqVO) {
        return bannerMapper.selectPage(pageReqVO);
    }

    @Override
    public void addBannerBrowseCount(Long id) {
        // 校验 Banner 是否存在
        validateBannerExists(id);
        // 增加点击次数
        bannerMapper.updateBrowseCount(id);
    }

    @Override
    public List<SalesBannerDO> getBannerListByPosition(Integer position) {
        return bannerMapper.selectBannerListByPosition(position);
    }

}
