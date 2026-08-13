package vip.appap.suxin.module.sales.convert;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.collection.CollectionUtils;
import vip.appap.suxin.framework.common.util.number.NumberUtils;
import vip.appap.suxin.framework.ip.core.utils.AreaUtils;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDeliveryPickUpStoreCreateReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDeliveryPickUpStoreRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDeliveryPickUpStoreSimpleRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDeliveryPickUpStoreUpdateReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesDeliveryyPickUpStoreRespVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDeliveryPickUpStoreDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface SalesDeliveryPickUpStoreConvert {

    SalesDeliveryPickUpStoreConvert INSTANCE = Mappers.getMapper(SalesDeliveryPickUpStoreConvert.class);

    SalesDeliveryPickUpStoreDO convert(SalesDeliveryPickUpStoreCreateReqVO bean);

    SalesDeliveryPickUpStoreDO convert(SalesDeliveryPickUpStoreUpdateReqVO bean);

    List<SalesDeliveryPickUpStoreRespVO> convertList(List<SalesDeliveryPickUpStoreDO> list);

    PageResult<SalesDeliveryPickUpStoreRespVO> convertPage(PageResult<SalesDeliveryPickUpStoreDO> page);

    List<SalesDeliveryPickUpStoreSimpleRespVO> convertList1(List<SalesDeliveryPickUpStoreDO> list);
    @Mapping(source = "areaId", target = "areaName", qualifiedByName = "convertAreaIdToAreaName")
    SalesDeliveryPickUpStoreSimpleRespVO convert02(SalesDeliveryPickUpStoreDO bean);

    @Named("convertAreaIdToAreaName")
    default String convertAreaIdToAreaName(Integer areaId) {
        return AreaUtils.format(areaId);
    }

    default List<AppSalesDeliveryyPickUpStoreRespVO> convertList(List<SalesDeliveryPickUpStoreDO> list,
                                                           Double latitude, Double longitude) {
        return CollectionUtils.convertList(list, store -> {
            AppSalesDeliveryyPickUpStoreRespVO storeVO = convert03(store);
            if (latitude != null && longitude != null) {
                storeVO.setDistance(NumberUtils.getDistance(latitude, longitude, storeVO.getLatitude(), storeVO.getLongitude()));
            }
            return storeVO;
        });
    }
    @Mapping(source = "areaId", target = "areaName", qualifiedByName = "convertAreaIdToAreaName")
    AppSalesDeliveryyPickUpStoreRespVO convert03(SalesDeliveryPickUpStoreDO bean);

}
