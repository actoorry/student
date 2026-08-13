package vip.appap.suxin.module.sales.api;

import vip.appap.suxin.module.sales.service.SalesBargainActivityService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;

/**
 * 砍价活动 Api 接口实现类
 *
 * @author HUIHUI
 */
@Service
@Validated
public class SalesBargainActivityApiImpl implements SalesBargainActivityApi {

    @Resource
    private SalesBargainActivityService bargainActivityService;

    @Override
    public void updateBargainActivityStock(Long id, Integer count) {
        bargainActivityService.updateBargainActivityStock(id, count);
    }

}
