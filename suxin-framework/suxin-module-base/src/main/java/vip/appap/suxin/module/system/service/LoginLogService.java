package vip.appap.suxin.module.system.service;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.system.api.dto.LoginLogCreateReqDTO;
import vip.appap.suxin.module.system.controller.admin.vo.LoginLogPageReqVO;
import vip.appap.suxin.module.system.dal.dataobject.LoginLogDO;

import jakarta.validation.Valid;

/**
 * 登录日志 Service 接口
 */
public interface LoginLogService {

    /**
     * 获得登录日志
     *
     * @param id 编号
     * @return 登录日志
     */
    LoginLogDO getLoginLog(Long id);

    /**
     * 获得登录日志分页
     *
     * @param pageReqVO 分页条件
     * @return 登录日志分页
     */
    PageResult<LoginLogDO> getLoginLogPage(LoginLogPageReqVO pageReqVO);

    /**
     * 创建登录日志
     *
     * @param reqDTO 日志信息
     */
    void createLoginLog(@Valid LoginLogCreateReqDTO reqDTO);

}
