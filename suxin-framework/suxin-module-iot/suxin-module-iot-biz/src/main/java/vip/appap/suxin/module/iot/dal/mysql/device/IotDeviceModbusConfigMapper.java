package vip.appap.suxin.module.iot.dal.mysql.device;

import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.iot.core.biz.dto.IotModbusDeviceConfigListReqDTO;
import vip.appap.suxin.module.iot.dal.dataobject.device.IotDeviceModbusConfigDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IoT 设备 Modbus 连接配置 Mapper
 *
 * @author 书心软件
 */
@Mapper
public interface IotDeviceModbusConfigMapper extends BaseMapperX<IotDeviceModbusConfigDO> {

    default IotDeviceModbusConfigDO selectByDeviceId(Long deviceId) {
        return selectOne(IotDeviceModbusConfigDO::getDeviceId, deviceId);
    }

    default List<IotDeviceModbusConfigDO> selectList(IotModbusDeviceConfigListReqDTO reqDTO) {
        return selectList(new LambdaQueryWrapperX<IotDeviceModbusConfigDO>()
                .eqIfPresent(IotDeviceModbusConfigDO::getStatus, reqDTO.getStatus())
                .eqIfPresent(IotDeviceModbusConfigDO::getMode, reqDTO.getMode())
                .inIfPresent(IotDeviceModbusConfigDO::getDeviceId, reqDTO.getDeviceIds()));
    }

}
