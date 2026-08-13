package vip.appap.suxin.module.hr.service.resume;

import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import vip.appap.suxin.module.hr.controller.admin.resume.vo.*;
import vip.appap.suxin.module.hr.dal.dataobject.resume.ResumeDO;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;

import vip.appap.suxin.module.hr.dal.mysql.resume.ResumeMapper;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.module.hr.enums.ErrorCodeConstants.*;

/**
 * 履历 Service 实现类
 *
 * 说明：履历只写入 hr_resume 表，不双写 partner 表。
 * 查询时通过 LEFT JOIN partner 获取员工姓名。
 *
 * @author admin
 */
@Service
@Validated
public class ResumeServiceImpl implements ResumeService {

    @Resource
    private ResumeMapper resumeMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createResume(ResumeSaveReqVO createReqVO) {
        ResumeDO resume = BeanUtils.toBean(createReqVO, ResumeDO.class);
        resumeMapper.insert(resume);
        return resume.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateResume(ResumeSaveReqVO updateReqVO) {
        validateResumeExists(updateReqVO.getId());
        ResumeDO updateObj = BeanUtils.toBean(updateReqVO, ResumeDO.class);
        resumeMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteResume(Long id) {
        validateResumeExists(id);
        resumeMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteResumeListByIds(List<Long> ids) {
        resumeMapper.deleteByIds(ids);
    }

    private void validateResumeExists(Long id) {
        if (resumeMapper.selectById(id) == null) {
            throw exception(RESUME_NOT_EXISTS);
        }
    }

    @Override
    public ResumeRespVO getResume(Long id) {
        ResumeRespVO resume = resumeMapper.selectByIdJoin(id);
        if (resume == null) {
            throw exception(RESUME_NOT_EXISTS);
        }
        return resume;
    }

    @Override
    public PageResult<ResumeRespVO> getResumePage(ResumePageReqVO pageReqVO) {
        return resumeMapper.selectPage(pageReqVO);
    }

}
