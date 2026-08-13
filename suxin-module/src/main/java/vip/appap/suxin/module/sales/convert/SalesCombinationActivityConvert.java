package vip.appap.suxin.module.sales.convert;

import cn.hutool.core.util.ObjectUtil;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.collection.CollectionUtils;
import vip.appap.suxin.framework.common.util.collection.MapUtils;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.partner.api.dto.PartnerRespDTO;
import vip.appap.suxin.module.product.api.dto.ProductSkuRespDTO;
import vip.appap.suxin.module.product.api.dto.ProductSpuRespDTO;
import vip.appap.suxin.module.sales.api.dto.SalesCombinationRecordCreateReqDTO;
import vip.appap.suxin.module.sales.api.dto.SalesCombinationRecordCreateRespDTO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesCombinationActivityCreateReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesCombinationActivityPageItemRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesCombinationActivityRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesCombinationActivityUpdateReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesCombinationProductBaseVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesCombinationProductRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesCombinationRecordPageItemRespVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesCombinationActivityDetailRespVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesCombinationActivityRespVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesCombinationRecordDetailRespVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesCombinationRecordRespVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesCombinationActivityDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesCombinationProductDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesCombinationRecordDO;
import vip.appap.suxin.module.sales.enums.SalesCombinationRecordStatusEnum;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.*;
import static vip.appap.suxin.framework.common.util.collection.MapUtils.findAndThen;

/**
 * 拼团活动 Convert
 *
 * @author HUIHUI
 */
@Mapper
public interface SalesCombinationActivityConvert {

    SalesCombinationActivityConvert INSTANCE = Mappers.getMapper(SalesCombinationActivityConvert.class);

    SalesCombinationActivityDO convert(SalesCombinationActivityCreateReqVO bean);

    SalesCombinationActivityDO convert(SalesCombinationActivityUpdateReqVO bean);

    SalesCombinationActivityRespVO convert(SalesCombinationActivityDO bean);

    SalesCombinationProductRespVO convert(SalesCombinationProductDO bean);

    default SalesCombinationActivityRespVO convert(SalesCombinationActivityDO activity, List<SalesCombinationProductDO> products) {
        return convert(activity).setProducts(convertList2(products));
    }

    List<SalesCombinationActivityRespVO> convertList(List<SalesCombinationActivityDO> list);

    default PageResult<SalesCombinationActivityPageItemRespVO> convertPage(PageResult<SalesCombinationActivityDO> page,
                                                                      List<SalesCombinationProductDO> productList,
                                                                      Map<Long, Integer> groupCountMap,
                                                                      Map<Long, Integer> groupSuccessCountMap,
                                                                      Map<Long, Integer> recordCountMap,
                                                                      List<ProductSpuRespDTO> spuList) {
        PageResult<SalesCombinationActivityPageItemRespVO> pageResult = convertPage(page);
        Map<Long, ProductSpuRespDTO> spuMap = convertMap(spuList, ProductSpuRespDTO::getId);
        Map<Long, List<SalesCombinationProductDO>> productMap = convertMultiMap(productList, SalesCombinationProductDO::getActivityId);
        pageResult.getList().forEach(item -> {
            MapUtils.findAndThen(spuMap, item.getSpuId(), spu -> item.setSpuName(spu.getName()).setPicUrl(spu.getPicUrl())
                    .setMarketPrice(spu.getMarketPrice()));
            item.setProducts(convertList2(productMap.get(item.getId())));
            // 设置统计字段
            item.setGroupCount(groupCountMap.getOrDefault(item.getId(), 0))
                    .setGroupSuccessCount(groupSuccessCountMap.getOrDefault(item.getId(), 0))
                    .setRecordCount(recordCountMap.getOrDefault(item.getId(), 0));
        });
        return pageResult;
    }

    PageResult<SalesCombinationActivityPageItemRespVO> convertPage(PageResult<SalesCombinationActivityDO> page);

    List<SalesCombinationProductRespVO> convertList2(List<SalesCombinationProductDO> productDOs);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "activityId", source = "activity.id"),
            @Mapping(target = "spuId", source = "activity.spuId"),
            @Mapping(target = "skuId", source = "product.skuId"),
            @Mapping(target = "combinationPrice", source = "product.combinationPrice"),
            @Mapping(target = "activityStartTime", source = "activity.startTime"),
            @Mapping(target = "activityEndTime", source = "activity.endTime")
    })
    SalesCombinationProductDO convert(SalesCombinationActivityDO activity, SalesCombinationProductBaseVO product);

    default List<SalesCombinationProductDO> convertList(List<? extends SalesCombinationProductBaseVO> products, SalesCombinationActivityDO activity) {
        return CollectionUtils.convertList(products, item -> convert(activity, item).setActivityStatus(activity.getStatus()));
    }

    default List<SalesCombinationProductDO> convertList(List<SalesCombinationProductBaseVO> updateProductVOs,
                                                   List<SalesCombinationProductDO> products, SalesCombinationActivityDO activity) {
        Map<Long, Long> productMap = convertMap(products, SalesCombinationProductDO::getSkuId, SalesCombinationProductDO::getId);
        return CollectionUtils.convertList(updateProductVOs, updateProductVO -> convert(activity, updateProductVO)
                .setId(productMap.get(updateProductVO.getSkuId()))
                .setActivityStatus(activity.getStatus()));
    }

    SalesCombinationRecordDO convert(SalesCombinationRecordCreateReqDTO reqDTO);

    default SalesCombinationRecordCreateRespDTO convert4(SalesCombinationRecordDO combinationRecord) {
        return new SalesCombinationRecordCreateRespDTO().setCombinationActivityId(combinationRecord.getActivityId())
                .setCombinationRecordId(combinationRecord.getId()).setCombinationHeadId(combinationRecord.getHeadId());
    }

    default SalesCombinationRecordDO convert(SalesCombinationRecordCreateReqDTO reqDTO,
                                        SalesCombinationActivityDO activity, PartnerRespDTO user,
                                        ProductSpuRespDTO spu, ProductSkuRespDTO sku) {
        return convert(reqDTO).setVirtualGroup(false)
                .setStatus(SalesCombinationRecordStatusEnum.IN_PROGRESS.getStatus()) // 创建后默认状态为进行中
                .setUserSize(activity.getUserSize()).setUserCount(1) // 默认就是 1 插入后会接着更新一次所有的拼团记录
                .setNickname(user.getNickname()).setAvatar(user.getAvatar()) // 用户信息
                .setSpuName(spu.getName()).setPicUrl(ObjectUtil.defaultIfBlank(sku.getPicUrl(), spu.getPicUrl())); // 商品信息
    }

    default List<SalesCombinationActivityRespVO> convertList(List<SalesCombinationActivityDO> list,
                                                        List<SalesCombinationProductDO> productList,
                                                        List<ProductSpuRespDTO> spuList) {
        List<SalesCombinationActivityRespVO> activityList = BeanUtils.toBean(list, SalesCombinationActivityRespVO.class);
        Map<Long, ProductSpuRespDTO> spuMap = convertMap(spuList, ProductSpuRespDTO::getId);
        Map<Long, List<SalesCombinationProductDO>> productMap = convertMultiMap(productList, SalesCombinationProductDO::getActivityId);
        return CollectionUtils.convertList(activityList, item -> {
            // 设置 product 信息
            item.setCombinationPrice(getMinValue(productMap.get(item.getId()), SalesCombinationProductDO::getCombinationPrice));
            // 设置 SPU 信息
            findAndThen(spuMap, item.getSpuId(), spu -> item.setSpuName(spu.getName())
                    .setPicUrl(spu.getPicUrl()).setMarketPrice(spu.getMarketPrice()));
            return item;
        });
    }

    default List<AppSalesCombinationActivityRespVO> convertAppList(List<SalesCombinationActivityDO> list,
                                                              List<SalesCombinationProductDO> productList,
                                                              List<ProductSpuRespDTO> spuList) {
        List<AppSalesCombinationActivityRespVO> activityList = BeanUtils.toBean(list, AppSalesCombinationActivityRespVO.class);
        Map<Long, ProductSpuRespDTO> spuMap = convertMap(spuList, ProductSpuRespDTO::getId);
        Map<Long, List<SalesCombinationProductDO>> productMap = convertMultiMap(productList, SalesCombinationProductDO::getActivityId);
        return CollectionUtils.convertList(activityList, item -> {
            // 设置 product 信息
            item.setCombinationPrice(getMinValue(productMap.get(item.getId()), SalesCombinationProductDO::getCombinationPrice));
            // 设置 SPU 信息
            findAndThen(spuMap, item.getSpuId(), spu -> item.setSpuName(spu.getName())
                    .setPicUrl(spu.getPicUrl()).setMarketPrice(spu.getMarketPrice()));
            return item;
        });
    }

    default PageResult<AppSalesCombinationActivityRespVO> convertAppPage(PageResult<SalesCombinationActivityDO> result,
                                                                    List<SalesCombinationProductDO> productList,
                                                                    List<ProductSpuRespDTO> spuList) {
        return new PageResult<>(convertAppList(result.getList(), productList, spuList), result.getTotal());
    }

    AppSalesCombinationActivityDetailRespVO convert2(SalesCombinationActivityDO combinationActivity);

    List<AppSalesCombinationActivityDetailRespVO.Product> convertList1(List<SalesCombinationProductDO> products);

    default AppSalesCombinationActivityDetailRespVO convert3(SalesCombinationActivityDO combinationActivity, List<SalesCombinationProductDO> products) {
        return convert2(combinationActivity).setProducts(convertList1(products));
    }

    List<AppSalesCombinationRecordRespVO> convertList3(List<SalesCombinationRecordDO> records);

    AppSalesCombinationRecordRespVO convert(SalesCombinationRecordDO record);

    PageResult<SalesCombinationRecordPageItemRespVO> convert(PageResult<SalesCombinationRecordDO> result);

    default PageResult<SalesCombinationRecordPageItemRespVO> convert(PageResult<SalesCombinationRecordDO> recordPage, List<SalesCombinationActivityDO> activities, List<SalesCombinationProductDO> products) {
        PageResult<SalesCombinationRecordPageItemRespVO> result = convert(recordPage);
        // 拼接关联属性
        Map<Long, SalesCombinationActivityDO> activityMap = convertMap(activities, SalesCombinationActivityDO::getId);
        Map<Long, List<SalesCombinationProductDO>> productsMap = convertMultiMap(products, SalesCombinationProductDO::getActivityId);
        result.setList(CollectionUtils.convertList(result.getList(), item -> {
            findAndThen(activityMap, item.getActivityId(), activity -> {
                item.setActivity(convert(activity).setProducts(convertList2(productsMap.get(item.getActivityId()))));
            });
            return item;
        }));
        return result;
    }

    default AppSalesCombinationRecordDetailRespVO convert(Long userId, SalesCombinationRecordDO headRecord, List<SalesCombinationRecordDO> PartnerRecords) {
        AppSalesCombinationRecordDetailRespVO respVO = new AppSalesCombinationRecordDetailRespVO()
                .setHeadRecord(convert(headRecord)).setMemberRecords(convertList3(PartnerRecords));
        // 处理自己参与拼团的 orderId
        SalesCombinationRecordDO userRecord = CollectionUtils.findFirst(PartnerRecords, r -> ObjectUtil.equal(r.getUserId(), userId));
        if (userRecord == null && ObjectUtil.equal(headRecord.getUserId(), userId)) {
            userRecord = headRecord;
        }
        respVO.setOrderId(userRecord == null ? null : userRecord.getOrderId());
        return respVO;
    }

    /**
     * 转换生成虚拟成团虚拟记录
     *
     * @param headRecord 虚拟成团团长记录
     * @return 虚拟记录列表
     */
    default List<SalesCombinationRecordDO> convertVirtualRecordList(SalesCombinationRecordDO headRecord) {
        int count = headRecord.getUserSize() - headRecord.getUserCount();
        List<SalesCombinationRecordDO> createRecords = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            // 基础信息和团长保持一致
            SalesCombinationRecordDO newRecord = convert5(headRecord).setHeadId(headRecord.getId());
            // 虚拟信息
            newRecord.setCount(0) // 会单独更新下，在后续的 Service 逻辑里
                    .setUserId(0L).setNickname("").setAvatar("").setOrderId(0L);
            createRecords.add(newRecord);
        }
        return createRecords;
    }
    @Mapping(target = "id", ignore = true)
    SalesCombinationRecordDO convert5(SalesCombinationRecordDO headRecord);

}
