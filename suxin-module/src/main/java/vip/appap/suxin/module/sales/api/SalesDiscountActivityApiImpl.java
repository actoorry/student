package vip.appap.suxin.module.sales.api;

import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.sales.api.dto.SalesDiscountProductRespDTO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDiscountProductDO;
import vip.appap.suxin.module.sales.service.SalesDiscountActivityService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

/**
 * 限时折扣 API 实现类
 *
 * @author 书心软件
 */
@Service
@Validated
public class SalesDiscountActivityApiImpl implements SalesDiscountActivityApi {

    @Resource
    private SalesDiscountActivityService discountActivityService;

    @Override
    public List<SalesDiscountProductRespDTO> getMatchDiscountProductListBySkuIds(Collection<Long> skuIds) {
        List<SalesDiscountProductDO> list = discountActivityService.getMatchDiscountProductListBySkuIds(skuIds);
        return BeanUtils.toBean(list, SalesDiscountProductRespDTO.class);
    }

}
