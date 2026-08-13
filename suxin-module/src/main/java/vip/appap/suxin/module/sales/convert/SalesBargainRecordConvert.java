package vip.appap.suxin.module.sales.convert;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.collection.CollectionUtils;
import vip.appap.suxin.framework.common.util.collection.MapUtils;
import vip.appap.suxin.module.partner.api.dto.PartnerRespDTO;
import vip.appap.suxin.module.product.api.dto.ProductSpuRespDTO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesBargainRecordPageItemRespVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesBargainRecordDetailRespVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesBargainRecordRespVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesBargainRecordSummaryRespVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesBargainActivityDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesBargainRecordDO;
import vip.appap.suxin.module.sales.api.dto.SalesOrderRespDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * 砍价记录 Convert
 *
 * @author 书心软件
 */
@Mapper
public interface SalesBargainRecordConvert {

    SalesBargainRecordConvert INSTANCE = Mappers.getMapper(SalesBargainRecordConvert.class);

    default PageResult<SalesBargainRecordPageItemRespVO> convertPage(PageResult<SalesBargainRecordDO> page,
                                                                Map<Long, Integer> helpCountMap,
                                                                List<SalesBargainActivityDO> activityList,
                                                                Map<Long, PartnerRespDTO> userMap) {
        PageResult<SalesBargainRecordPageItemRespVO> pageResult = convertPage(page);
        // 拼接数据
        Map<Long, SalesBargainActivityDO> activityMap = convertMap(activityList, SalesBargainActivityDO::getId);
        pageResult.getList().forEach(record -> {
            MapUtils.findAndThen(userMap, record.getUserId(),
                    user -> record.setNickname(user.getNickname()).setAvatar(user.getAvatar()));
            record.setActivity(SalesBargainActivityConvert.INSTANCE.convert(activityMap.get(record.getActivityId())))
                    .setHelpCount(helpCountMap.getOrDefault(record.getId(), 0));
        });
        return pageResult;
    }
    PageResult<SalesBargainRecordPageItemRespVO> convertPage(PageResult<SalesBargainRecordDO> page);

    default PageResult<AppSalesBargainRecordRespVO> convertPage02(PageResult<SalesBargainRecordDO> page,
                                                             List<SalesBargainActivityDO> activityList,
                                                             List<ProductSpuRespDTO> spuList,
                                                             List<SalesOrderRespDTO> orderList) {
        PageResult<AppSalesBargainRecordRespVO> pageResult = convertPage02(page);
        // 拼接数据
        Map<Long, SalesBargainActivityDO> activityMap = convertMap(activityList, SalesBargainActivityDO::getId);
        Map<Long, ProductSpuRespDTO> spuMap = convertMap(spuList, ProductSpuRespDTO::getId);
        Map<Long, SalesOrderRespDTO> orderMap = convertMap(orderList, SalesOrderRespDTO::getId);
        pageResult.getList().forEach(record -> {
            MapUtils.findAndThen(activityMap, record.getActivityId(),
                    activity -> record.setActivityName(activity.getName()).setEndTime(activity.getEndTime()));
            MapUtils.findAndThen(spuMap, record.getSpuId(),
                    spu -> record.setPicUrl(record.getPicUrl()));
            MapUtils.findAndThen(orderMap, record.getOrderId(),
                    order -> record.setPayStatus(order.getPayStatus()).setPayOrderId(order.getPayOrderId()));
        });
        return pageResult;
    }
    PageResult<AppSalesBargainRecordRespVO> convertPage02(PageResult<SalesBargainRecordDO> page);

    default AppSalesBargainRecordSummaryRespVO convert(Integer successUserCount, List<SalesBargainRecordDO> successList,
                                                  List<SalesBargainActivityDO> activityList, Map<Long, PartnerRespDTO> userMap) {
        AppSalesBargainRecordSummaryRespVO summary = new AppSalesBargainRecordSummaryRespVO().setSuccessUserCount(successUserCount);
        Map<Long, SalesBargainActivityDO> activityMap = convertMap(activityList, SalesBargainActivityDO::getId);
        summary.setSuccessList(CollectionUtils.convertList(successList, record -> {
            AppSalesBargainRecordSummaryRespVO.Record recordVO = new AppSalesBargainRecordSummaryRespVO.Record();
            MapUtils.findAndThen(userMap, record.getUserId(),
                    user -> recordVO.setNickname(user.getNickname()).setAvatar(user.getAvatar()));
            MapUtils.findAndThen(activityMap, record.getActivityId(),
                    activity -> recordVO.setActivityName(activity.getName()));
            return recordVO;
        }));
        return summary;
    }

    @Mapping(source = "record.id", target = "id")
    @Mapping(source = "record.userId", target = "userId")
    @Mapping(source = "record.status", target = "status")
    AppSalesBargainRecordDetailRespVO convert02(SalesBargainRecordDO record, Integer helpAction, SalesOrderRespDTO order);

}
