package vip.appap.suxin.module.iot.core.topic.service;

import vip.appap.suxin.module.iot.core.enums.IotDeviceMessageMethodEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * IoT 设备服务调用 Request DTO
 * <p>
 * 用于 {@link IotDeviceMessageMethodEnum#SERVICE_INVOKE} 下行消息的 params 参数
 *
 * @author 书心软件
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IotDeviceServiceInvokeReqDTO {

    /**
     * 服务标识符
     */
    private String identifier;

    /**
     * 服务输入参数
     */
    private Map<String, Object> inputParams;

}
