package vip.appap.suxin.module.product.api;

import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.product.api.dto.ProductSpuRespDTO;
import vip.appap.suxin.module.product.dal.dataobject.ProductSpuDO;
import vip.appap.suxin.module.product.service.ProductSpuService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

/**
 * 商品 SPU API 接口实现类
 *
 * @author LeeYan9
 * @since 2022-09-06
 */
@Service
@Validated
public class ProductSpuApiImpl implements ProductSpuApi {

    @Resource
    private ProductSpuService spuService;

    @Override
    public List<ProductSpuRespDTO> getSpuList(Collection<Long> ids) {
        List<ProductSpuDO> spus = spuService.getSpuList(ids);
        return BeanUtils.toBean(spus, ProductSpuRespDTO.class);
    }

    @Override
    public List<ProductSpuRespDTO> validateSpuList(Collection<Long> ids) {
        List<ProductSpuDO> spus = spuService.validateSpuList(ids);
        return BeanUtils.toBean(spus, ProductSpuRespDTO.class);
    }

    @Override
    public ProductSpuRespDTO getSpu(Long id) {
        ProductSpuDO spu = spuService.getSpu(id);
        return BeanUtils.toBean(spu, ProductSpuRespDTO.class);
    }

    @Override
    public List<ProductSpuRespDTO> getSpuListByName(String name, Integer limit) {
        List<ProductSpuDO> spus = spuService.getSpuListByName(name, limit);
        return BeanUtils.toBean(spus, ProductSpuRespDTO.class);
    }

}
