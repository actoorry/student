package vip.appap.suxin.module.iot.dal.mysql.device;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.iot.controller.admin.device.vo.group.IotDeviceGroupPageReqVO;
import vip.appap.suxin.module.iot.dal.dataobject.device.IotDeviceGroupDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IoT 设备分组 Mapper
 *
 * @author 书心软件
 */
@Mapper
public interface IotDeviceGroupMapper extends BaseMapperX<IotDeviceGroupDO> {

    default PageResult<IotDeviceGroupDO> selectPage(IotDeviceGroupPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotDeviceGroupDO>()
                .likeIfPresent(IotDeviceGroupDO::getName, reqVO.getName())
                .betweenIfPresent(IotDeviceGroupDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(IotDeviceGroupDO::getId));
    }

    default List<IotDeviceGroupDO> selectListByStatus(Integer status) {
        return selectList(IotDeviceGroupDO::getStatus, status);
    }

    default IotDeviceGroupDO selectByName(String name) {
        return selectOne(IotDeviceGroupDO::getName, name);
    }

}