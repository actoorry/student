package vip.appap.suxin.module.campus.service;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.campus.controller.admin.vo.TeacherPageReqVO;
import vip.appap.suxin.module.campus.controller.admin.vo.TeacherSaveReqVO;
import vip.appap.suxin.module.campus.dal.dataobject.TeacherDO;
import vip.appap.suxin.module.campus.dal.mysql.TeacherMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.module.campus.enums.ErrorCodeConstants.TEACHER_NOT_EXISTS;
import static vip.appap.suxin.module.campus.enums.ErrorCodeConstants.TEACHER_NO_EXISTS;

/**
 * 教师 Service 实现类
 *
 * @author 书心软件
 */
@Service
@Validated
public class TeacherServiceImpl implements TeacherService {

    @Resource
    private TeacherMapper teacherMapper;

    @Override
    public Long createTeacher(TeacherSaveReqVO createReqVO) {
        // 校验工号唯一
        validateTeacherNoUnique(null, createReqVO.getTeacherNo());
        // 插入
        TeacherDO teacher = BeanUtils.toBean(createReqVO, TeacherDO.class);
        teacherMapper.insert(teacher);
        return teacher.getId();
    }

    @Override
    public void updateTeacher(TeacherSaveReqVO updateReqVO) {
        // 校验存在
        validateTeacherExists(updateReqVO.getId());
        // 校验工号唯一
        validateTeacherNoUnique(updateReqVO.getId(), updateReqVO.getTeacherNo());
        // 更新
        TeacherDO updateObj = BeanUtils.toBean(updateReqVO, TeacherDO.class);
        teacherMapper.updateById(updateObj);
    }

    @Override
    public void deleteTeacher(Long id) {
        // 校验存在
        validateTeacherExists(id);
        // 删除
        teacherMapper.deleteById(id);
    }

    @Override
    public TeacherDO getTeacher(Long id) {
        return teacherMapper.selectById(id);
    }

    @Override
    public PageResult<TeacherDO> getTeacherPage(TeacherPageReqVO pageReqVO) {
        return teacherMapper.selectPage(pageReqVO);
    }

    @Override
    public List<TeacherDO> getTeacherList(Collection<Long> ids) {
        return teacherMapper.selectBatchIds(ids);
    }

    @Override
    public TeacherDO validateTeacherExists(Long id) {
        TeacherDO teacher = teacherMapper.selectById(id);
        if (teacher == null) {
            throw exception(TEACHER_NOT_EXISTS);
        }
        return teacher;
    }

    private void validateTeacherNoUnique(Long id, String teacherNo) {
        TeacherDO teacher = teacherMapper.selectByTeacherNo(teacherNo);
        if (teacher == null) {
            return;
        }
        if (id == null || !id.equals(teacher.getId())) {
            throw exception(TEACHER_NO_EXISTS, teacherNo);
        }
    }

}
