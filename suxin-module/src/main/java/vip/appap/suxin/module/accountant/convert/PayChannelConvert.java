package vip.appap.suxin.module.accountant.convert;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.accountant.controller.admin.vo.PayChannelCreateReqVO;
import vip.appap.suxin.module.accountant.controller.admin.vo.PayChannelRespVO;
import vip.appap.suxin.module.accountant.controller.admin.vo.PayChannelUpdateReqVO;
import vip.appap.suxin.module.accountant.dal.dataobject.PayChannelDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PayChannelConvert {

    PayChannelConvert INSTANCE = Mappers.getMapper(PayChannelConvert.class);

    @Mapping(target = "config",ignore = true)
    PayChannelDO convert(PayChannelCreateReqVO bean);

    @Mapping(target = "config",ignore = true)
    PayChannelDO convert(PayChannelUpdateReqVO bean);

    @Mapping(target = "config",expression = "java(vip.appap.suxin.framework.common.util.json.JsonUtils.toJsonString(bean.getConfig()))")
    PayChannelRespVO convert(PayChannelDO bean);

    PageResult<PayChannelRespVO> convertPage(PageResult<PayChannelDO> page);

}

