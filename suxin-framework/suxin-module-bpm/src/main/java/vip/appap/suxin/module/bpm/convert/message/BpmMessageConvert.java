package vip.appap.suxin.module.bpm.convert.message;

import vip.appap.suxin.module.system.api.dto.SmsSendSingleToUserReqDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.Map;

@Mapper
public interface BpmMessageConvert {

    BpmMessageConvert INSTANCE = Mappers.getMapper(BpmMessageConvert.class);

    @Mapping(target = "mobile", ignore = true)
    @Mapping(source = "userId", target = "userId")
    @Mapping(source = "templateCode", target = "templateCode")
    @Mapping(source = "templateParams", target = "templateParams")
    SmsSendSingleToUserReqDTO convert(Long userId, String templateCode, Map<String, Object> templateParams);

}
