package vip.appap.suxin.module.sales.convert;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.collection.CollectionUtils;
import vip.appap.suxin.framework.common.util.collection.MapUtils;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.product.api.dto.ProductSpuRespDTO;
import vip.appap.suxin.module.sales.api.dto.SalesSeckillValidateJoinRespDTO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesSeckillActivityCreateReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesSeckillActivityDetailRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesSeckillActivityRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesSeckillActivityUpdateReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesSeckillProductBaseVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesSeckillProductRespVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesSeckillActivityDetailRespVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesSeckillActivityNowRespVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesSeckillActivityRespVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesSeckillActivityDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesSeckillConfigDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesSeckillProductDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.*;
import static vip.appap.suxin.framework.common.util.collection.MapUtils.findAndThen;

/**
 * 秒杀活动 Convert
 *
 * @author 书心软件
 */
@Mapper
public interface SalesSeckillActivityConvert {

    SalesSeckillActivityConvert INSTANCE = Mappers.getMapper(SalesSeckillActivityConvert.class);

    SalesSeckillActivityDO convert(SalesSeckillActivityCreateReqVO bean);

    SalesSeckillActivityDO convert(SalesSeckillActivityUpdateReqVO bean);

    SalesSeckillActivityRespVO convert(SalesSeckillActivityDO bean);

    List<SalesSeckillActivityRespVO> convertList(List<SalesSeckillActivityDO> list);

    PageResult<SalesSeckillActivityRespVO> convertPage(PageResult<SalesSeckillActivityDO> page);

    default PageResult<SalesSeckillActivityRespVO> convertPage(PageResult<SalesSeckillActivityDO> page,
                                                          List<SalesSeckillProductDO> seckillProducts,
                                                          List<ProductSpuRespDTO> spuList) {
        PageResult<SalesSeckillActivityRespVO> pageResult = convertPage(page);
        // 拼接商品
        Map<Long, ProductSpuRespDTO> spuMap = CollectionUtils.convertMap(spuList, ProductSpuRespDTO::getId);
        Map<Long, List<SalesSeckillProductDO>> productMap = convertMultiMap(seckillProducts, SalesSeckillProductDO::getActivityId);
        pageResult.getList().forEach(activity -> {
            activity.setProducts(convertList2(productMap.get(activity.getId())));
            MapUtils.findAndThen(spuMap, activity.getSpuId(),
                    spu -> activity.setSpuName(spu.getName()).setPicUrl(spu.getPicUrl()).setMarketPrice(spu.getMarketPrice()));
        });
        return pageResult;
    }

    SalesSeckillActivityDetailRespVO convert1(SalesSeckillActivityDO activity);

    default SalesSeckillActivityDetailRespVO convert(SalesSeckillActivityDO activity, List<SalesSeckillProductDO> products) {
        return convert1(activity).setProducts(convertList2(products));
    }

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "activityId", source = "activity.id"),
            @Mapping(target = "configIds", source = "activity.configIds"),
            @Mapping(target = "spuId", source = "activity.spuId"),
            @Mapping(target = "skuId", source = "product.skuId"),
            @Mapping(target = "seckillPrice", source = "product.seckillPrice"),
            @Mapping(target = "stock", source = "product.stock"),
            @Mapping(target = "activityStartTime", source = "activity.startTime"),
            @Mapping(target = "activityEndTime", source = "activity.endTime")
    })
    SalesSeckillProductDO convert(SalesSeckillActivityDO activity, SalesSeckillProductBaseVO product);

    default List<SalesSeckillProductDO> convertList(List<? extends SalesSeckillProductBaseVO> products, SalesSeckillActivityDO activity) {
        return CollectionUtils.convertList(products, item -> convert(activity, item).setActivityStatus(activity.getStatus()));
    }

    default List<SalesSeckillActivityRespVO> convertList(List<SalesSeckillActivityDO> list,
                                                        List<SalesSeckillProductDO> productList,
                                                        List<ProductSpuRespDTO> spuList) {
        List<SalesSeckillActivityRespVO> activityList = BeanUtils.toBean(list, SalesSeckillActivityRespVO.class);
        Map<Long, ProductSpuRespDTO> spuMap = convertMap(spuList, ProductSpuRespDTO::getId);
        Map<Long, List<SalesSeckillProductDO>> productMap = convertMultiMap(productList, SalesSeckillProductDO::getActivityId);
        return CollectionUtils.convertList(activityList, item -> {
            // 设置 product 信息
            item.setSeckillPrice(getMinValue(productMap.get(item.getId()), SalesSeckillProductDO::getSeckillPrice));
            // 设置 SPU 信息
            findAndThen(spuMap, item.getSpuId(), spu -> item.setSpuName(spu.getName())
                    .setPicUrl(spu.getPicUrl()).setMarketPrice(spu.getMarketPrice()));
            return item;
        });
    }

    default List<AppSalesSeckillActivityRespVO> convertAppList(List<SalesSeckillActivityDO> list,
                                                              List<SalesSeckillProductDO> productList,
                                                              List<ProductSpuRespDTO> spuList) {
        List<AppSalesSeckillActivityRespVO> activityList = BeanUtils.toBean(list, AppSalesSeckillActivityRespVO.class);
        Map<Long, ProductSpuRespDTO> spuMap = convertMap(spuList, ProductSpuRespDTO::getId);
        Map<Long, List<SalesSeckillProductDO>> productMap = convertMultiMap(productList, SalesSeckillProductDO::getActivityId);
        return CollectionUtils.convertList(activityList, item -> {
            // 设置 product 信息
            item.setSeckillPrice(getMinValue(productMap.get(item.getId()), SalesSeckillProductDO::getSeckillPrice));
            // 设置 SPU 信息
            findAndThen(spuMap, item.getSpuId(), spu -> item.setSpuName(spu.getName())
                    .setPicUrl(spu.getPicUrl()).setMarketPrice(spu.getMarketPrice()));
            return item;
        });
    }

    List<SalesSeckillProductRespVO> convertList2(List<SalesSeckillProductDO> list);

    List<AppSalesSeckillActivityRespVO> convertList3(List<SalesSeckillActivityDO> activityList);

    default AppSalesSeckillActivityNowRespVO convert(SalesSeckillConfigDO filteredConfig, List<SalesSeckillActivityDO> activityList,
                                                List<SalesSeckillProductDO> productList, List<ProductSpuRespDTO> spuList) {
        AppSalesSeckillActivityNowRespVO respVO = new AppSalesSeckillActivityNowRespVO();
        respVO.setConfig(SalesSeckillConfigConvert.INSTANCE.convert1(filteredConfig));
        Map<Long, ProductSpuRespDTO> spuMap = convertMap(spuList, ProductSpuRespDTO::getId);
        Map<Long, List<SalesSeckillProductDO>> productMap = convertMultiMap(productList, SalesSeckillProductDO::getActivityId);
        respVO.setActivities(CollectionUtils.convertList(convertList3(activityList), item -> {
            // product 信息
            item.setSeckillPrice(getMinValue(productMap.get(item.getId()), SalesSeckillProductDO::getSeckillPrice));
            // spu 信息
            findAndThen(spuMap, item.getSpuId(), spu ->
                    item.setPicUrl(spu.getPicUrl()).setMarketPrice(spu.getMarketPrice()));
            return item;
        }));
        return respVO;
    }

    PageResult<AppSalesSeckillActivityRespVO> convertPage1(PageResult<SalesSeckillActivityDO> pageResult);

    default PageResult<AppSalesSeckillActivityRespVO> convertPage02(PageResult<SalesSeckillActivityDO> pageResult, List<SalesSeckillProductDO> productList, List<ProductSpuRespDTO> spuList) {
        PageResult<AppSalesSeckillActivityRespVO> result = convertPage1(pageResult);
        Map<Long, ProductSpuRespDTO> spuMap = convertMap(spuList, ProductSpuRespDTO::getId);
        Map<Long, List<SalesSeckillProductDO>> productMap = convertMultiMap(productList, SalesSeckillProductDO::getActivityId);
        List<AppSalesSeckillActivityRespVO> list = CollectionUtils.convertList(result.getList(), item -> {
            // product 信息
            item.setSeckillPrice(getMinValue(productMap.get(item.getId()), SalesSeckillProductDO::getSeckillPrice));
            // spu 信息
            findAndThen(spuMap, item.getSpuId(), spu -> item.setPicUrl(spu.getPicUrl()).setMarketPrice(spu.getMarketPrice()));
            return item;
        });
        result.setList(list);
        return result;
    }

    AppSalesSeckillActivityDetailRespVO convert2(SalesSeckillActivityDO seckillActivity);

    List<AppSalesSeckillActivityDetailRespVO.Product> convertList1(List<SalesSeckillProductDO> products);

    default AppSalesSeckillActivityDetailRespVO convert3(SalesSeckillActivityDO activity, List<SalesSeckillProductDO> products,
                                                    LocalDateTime startTime, LocalDateTime endTime) {
        return convert2(activity)
                .setProducts(convertList1(products))
                .setStartTime(startTime).setEndTime(endTime);
    }

    SalesSeckillValidateJoinRespDTO convert02(SalesSeckillActivityDO activity, SalesSeckillProductDO product);

}
