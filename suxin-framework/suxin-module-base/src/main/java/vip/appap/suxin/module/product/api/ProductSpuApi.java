package vip.appap.suxin.module.product.api;

import vip.appap.suxin.module.product.api.dto.ProductSpuRespDTO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * 商品 SPU API 接口
 *
 * @author LeeYan9
 * @since 2022-08-26
 */
public interface ProductSpuApi {

    /**
     * 批量查询 SPU 数组
     *
     * @param ids SPU 编号列表
     * @return SPU 数组
     */
    List<ProductSpuRespDTO> getSpuList(Collection<Long> ids);

    /**
     * 批量查询 SPU MAP
     *
     * @param ids SPU 编号列表
     * @return SPU MAP
     */
    default Map<Long, ProductSpuRespDTO> getSpuMap(Collection<Long> ids) {
        return convertMap(getSpuList(ids), ProductSpuRespDTO::getId);
    }

    /**
     * 批量查询 SPU 数组，并且校验是否 SPU 是否有效。
     *
     * 如下情况，视为无效：
     * 1. 商品编号不存在
     * 2. 商品被禁用
     *
     * @param ids SPU 编号列表
     * @return SPU 数组
     */
    List<ProductSpuRespDTO> validateSpuList(Collection<Long> ids);

    /**
     * 获得 SPU
     *
     * @return SPU
     */
    ProductSpuRespDTO getSpu(Long id);

    /**
     * 基于商品名称模糊搜索 SPU 列表
     *
     * @param name 商品名称（模糊匹配）
     * @param limit 返回条数上限
     * @return SPU 列表
     */
    List<ProductSpuRespDTO> getSpuListByName(String name, Integer limit);

}
