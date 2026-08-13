package vip.appap.suxin.module.system.api;

import vip.appap.suxin.framework.common.biz.system.logger.OperateLogCommonApi;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.system.api.dto.OperateLogPageReqDTO;
import vip.appap.suxin.module.system.api.dto.OperateLogRespDTO;

/**
 * 操作日志 API 接口
 *
 * @author 书心软件
 */
public interface OperateLogApi extends OperateLogCommonApi {

    /**
     * 获取指定模块的指定数据的操作日志分页
     *
     * @param pageReqDTO 请求
     * @return 操作日志分页
     */
    PageResult<OperateLogRespDTO> getOperateLogPage(OperateLogPageReqDTO pageReqDTO);

}
