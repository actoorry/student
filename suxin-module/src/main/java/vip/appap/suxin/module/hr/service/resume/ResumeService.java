package vip.appap.suxin.module.hr.service.resume;

import jakarta.validation.*;
import vip.appap.suxin.module.hr.controller.admin.resume.vo.*;
import vip.appap.suxin.framework.common.pojo.PageResult;

/**
 * 履历 Service 接口
 *
 * @author admin
 */
public interface ResumeService {

    /**
     * 创建履历
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createResume(@Valid ResumeSaveReqVO createReqVO);

    /**
     * 更新履历
     *
     * @param updateReqVO 更新信息
     */
    void updateResume(@Valid ResumeSaveReqVO updateReqVO);

    /**
     * 删除履历
     *
     * @param id 编号
     */
    void deleteResume(Long id);

    /**
     * 批量删除履历
     *
     * @param ids 编号
     */
    void deleteResumeListByIds(java.util.List<Long> ids);

    /**
     * 获得履历
     *
     * @param id 编号
     * @return 履历
     */
    ResumeRespVO getResume(Long id);

    /**
     * 获得履历分页
     *
     * @param pageReqVO 分页查询
     * @return 履历分页
     */
    PageResult<ResumeRespVO> getResumePage(ResumePageReqVO pageReqVO);

}
