package vip.appap.suxin.module.system.convert;

import vip.appap.suxin.module.system.controller.admin.vo.TenantSaveReqVO;
import vip.appap.suxin.module.system.controller.admin.vo.UserSaveReqVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 租户 Convert
 *
 * @author 书心软件
 */
@Mapper
public interface TenantConvert {

    TenantConvert INSTANCE = Mappers.getMapper(TenantConvert.class);

    default UserSaveReqVO convert02(TenantSaveReqVO bean, Long partnerId) {
        UserSaveReqVO reqVO = new UserSaveReqVO();
        reqVO.setUsername(bean.getUsername());
        reqVO.setPassword(bean.getPassword());
        reqVO.setPartnerId(partnerId);
        return reqVO;
    }

}
