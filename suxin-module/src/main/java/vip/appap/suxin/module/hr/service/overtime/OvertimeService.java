package vip.appap.suxin.module.hr.service.overtime;

import java.util.*;
import jakarta.validation.*;
import vip.appap.suxin.module.hr.controller.admin.overtime.vo.*;
import vip.appap.suxin.framework.common.pojo.PageResult;

/**
 * 加班登记 Service 接口
 *
 * @author admin
 */
public interface OvertimeService {

    /**
     * 创建加班登记
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createOvertime(@Valid OvertimeSaveReqVO createReqVO);

    /**
     * 更新加班登记
     *
     * @param updateReqVO 更新信息
     */
    void updateOvertime(@Valid OvertimeSaveReqVO updateReqVO);

    /**
     * 删除加班登记
     *
     * @param id 编号
     */
    void deleteOvertime(Long id);

    /**
     * 批量删除加班登记
     *
     * @param ids 编号
     */
    void deleteOvertimeListByIds(List<Long> ids);

    /**
     * 获得加班登记
     *
     * @param id 编号
     * @return 加班登记
     */
    OvertimeRespVO getOvertime(Long id);

    /**
     * 获得加班登记分页
     *
     * @param pageReqVO 分页查询
     * @return 加班登记分页
     */
    PageResult<OvertimeRespVO> getOvertimePage(OvertimePageReqVO pageReqVO);

}
