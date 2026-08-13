package vip.appap.suxin.module.sales.convert;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.collection.CollectionUtils;
import vip.appap.suxin.module.product.api.dto.ProductSpuRespDTO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesBargainActivityBaseVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesBargainActivityPageItemRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesBargainActivityRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesBargainActivityUpdateReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesBargainActivityDetailRespVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesBargainActivityRespVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesBargainActivityDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertMap;
import static vip.appap.suxin.framework.common.util.collection.MapUtils.findAndThen;

/**
 * 拼团活动 Convert
 *
 * @author HUIHUI
 */
@Mapper
public interface SalesBargainActivityConvert {

    SalesBargainActivityConvert INSTANCE = Mappers.getMapper(SalesBargainActivityConvert.class);

    SalesBargainActivityDO convert(SalesBargainActivityBaseVO bean);

    SalesBargainActivityDO convert(SalesBargainActivityUpdateReqVO bean);

    SalesBargainActivityRespVO convert(SalesBargainActivityDO bean);

    List<SalesBargainActivityRespVO> convertList(List<SalesBargainActivityDO> list);

    PageResult<SalesBargainActivityPageItemRespVO> convertPage(PageResult<SalesBargainActivityDO> page);

    default PageResult<SalesBargainActivityPageItemRespVO> convertPage(PageResult<SalesBargainActivityDO> page, List<ProductSpuRespDTO> spuList,
                                                                  Map<Long, Integer> recordUserCountMap, Map<Long, Integer> recordSuccessUserCountMap,
                                                                  Map<Long, Integer> helpUserCountMap) {
        PageResult<SalesBargainActivityPageItemRespVO> result = convertPage(page);
        // 拼接关联属性
        Map<Long, ProductSpuRespDTO> spuMap = convertMap(spuList, ProductSpuRespDTO::getId);
        result.getList().forEach(item -> {
            findAndThen(spuMap, item.getSpuId(), spu -> {
                item.setPicUrl(spu.getPicUrl()).setSpuName(spu.getName());
            });
            // 设置统计字段
            item.setRecordUserCount(recordUserCountMap.getOrDefault(item.getId(), 0))
                    .setRecordSuccessUserCount(recordSuccessUserCountMap.getOrDefault(item.getId(), 0))
                    .setHelpUserCount(helpUserCountMap.getOrDefault(item.getId(), 0));
        });
        return result;
    }

    AppSalesBargainActivityDetailRespVO convert1(SalesBargainActivityDO bean);

    default AppSalesBargainActivityDetailRespVO convert(SalesBargainActivityDO bean, Integer successUserCount, ProductSpuRespDTO spu) {
        AppSalesBargainActivityDetailRespVO detail = convert1(bean).setSuccessUserCount(successUserCount);
        if (spu != null) {
            detail.setPicUrl(spu.getPicUrl()).setMarketPrice(spu.getMarketPrice());
        }
        return detail;
    }

    PageResult<AppSalesBargainActivityRespVO> convertAppPage(PageResult<SalesBargainActivityDO> page);

    default PageResult<AppSalesBargainActivityRespVO> convertAppPage(PageResult<SalesBargainActivityDO> page, List<ProductSpuRespDTO> spuList) {
        PageResult<AppSalesBargainActivityRespVO> result = convertAppPage(page);
        // 拼接关联属性
        Map<Long, ProductSpuRespDTO> spuMap = convertMap(spuList, ProductSpuRespDTO::getId);
        List<AppSalesBargainActivityRespVO> list = CollectionUtils.convertList(result.getList(), item -> {
            findAndThen(spuMap, item.getSpuId(), spu -> item.setPicUrl(spu.getPicUrl()).setMarketPrice(spu.getMarketPrice()));
            return item;
        });
        result.setList(list);
        return result;
    }

    List<AppSalesBargainActivityRespVO> convertAppList(List<SalesBargainActivityDO> list);

    default List<AppSalesBargainActivityRespVO> convertAppList(List<SalesBargainActivityDO> list, List<ProductSpuRespDTO> spuList) {
        List<AppSalesBargainActivityRespVO> activityList = convertAppList(list);
        Map<Long, ProductSpuRespDTO> spuMap = convertMap(spuList, ProductSpuRespDTO::getId);
        return CollectionUtils.convertList(activityList, item -> {
            findAndThen(spuMap, item.getSpuId(), spu -> item.setPicUrl(spu.getPicUrl()).setMarketPrice(spu.getMarketPrice()));
            return item;
        });
    }

}
