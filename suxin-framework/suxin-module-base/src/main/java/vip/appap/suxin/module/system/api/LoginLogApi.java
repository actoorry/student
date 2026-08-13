package vip.appap.suxin.module.system.api;

import vip.appap.suxin.module.system.api.dto.LoginLogCreateReqDTO;

import jakarta.validation.Valid;

/**
 * 登录日志的 API 接口
 *
 * @author 书心软件
 */
public interface LoginLogApi {

    /**
     * 创建登录日志
     *
     * @param reqDTO 日志信息
     */
    void createLoginLog(@Valid LoginLogCreateReqDTO reqDTO);

}
