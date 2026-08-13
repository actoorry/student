package vip.appap.suxin.module.system.service;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.biz.system.logger.dto.OperateLogCreateReqDTO;
import vip.appap.suxin.module.system.api.dto.OperateLogPageReqDTO;
import vip.appap.suxin.module.system.controller.admin.vo.OperateLogPageReqVO;
import vip.appap.suxin.module.system.dal.dataobject.OperateLogDO;

/**
 * 操作日志 Service 接口
 *
 * @author 书心软件
 */
public interface OperateLogService {

    /**
     * 记录操作日志
     *
     * @param createReqDTO 创建请求
     */
    void createOperateLog(OperateLogCreateReqDTO createReqDTO);

    /**
     * 获得操作日志
     *
     * @param id 编号
     * @return 操作日志
     */
    OperateLogDO getOperateLog(Long id);

    /**
     * 获得操作日志分页列表
     *
     * @param pageReqVO 分页条件
     * @return 操作日志分页列表
     */
    PageResult<OperateLogDO> getOperateLogPage(OperateLogPageReqVO pageReqVO);

    /**
     * 获得操作日志分页列表
     *
     * @param pageReqVO 分页条件
     * @return 操作日志分页列表
     */
    PageResult<OperateLogDO> getOperateLogPage(OperateLogPageReqDTO pageReqVO);

}
