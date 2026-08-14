package vip.appap.suxin.module.campus.service;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.campus.controller.admin.vo.StudentPageReqVO;
import vip.appap.suxin.module.campus.controller.admin.vo.StudentSaveReqVO;
import vip.appap.suxin.module.campus.dal.dataobject.StudentDO;
import vip.appap.suxin.module.campus.dal.mysql.StudentMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.module.campus.enums.ErrorCodeConstants.STUDENT_NOT_EXISTS;
import static vip.appap.suxin.module.campus.enums.ErrorCodeConstants.STUDENT_NO_EXISTS;

/**
 * 学生 Service 实现类
 *
 * @author 书心软件
 */
@Service
@Validated
public class StudentServiceImpl implements StudentService {

    @Resource
    private StudentMapper studentMapper;

    @Override
    public Long createStudent(StudentSaveReqVO createReqVO) {
        // 校验学号唯一
        validateStudentNoUnique(null, createReqVO.getStudentNo());
        // 插入
        StudentDO student = BeanUtils.toBean(createReqVO, StudentDO.class);
        studentMapper.insert(student);
        return student.getId();
    }

    @Override
    public void updateStudent(StudentSaveReqVO updateReqVO) {
        // 校验存在
        validateStudentExists(updateReqVO.getId());
        // 校验学号唯一
        validateStudentNoUnique(updateReqVO.getId(), updateReqVO.getStudentNo());
        // 更新
        StudentDO updateObj = BeanUtils.toBean(updateReqVO, StudentDO.class);
        studentMapper.updateById(updateObj);
    }

    @Override
    public void deleteStudent(Long id) {
        // 校验存在
        validateStudentExists(id);
        // 删除
        studentMapper.deleteById(id);
    }

    @Override
    public StudentDO getStudent(Long id) {
        return studentMapper.selectById(id);
    }

    @Override
    public PageResult<StudentDO> getStudentPage(StudentPageReqVO pageReqVO) {
        return studentMapper.selectPage(pageReqVO);
    }

    @Override
    public List<StudentDO> getStudentList(Collection<Long> ids) {
        return studentMapper.selectBatchIds(ids);
    }

    @Override
    public StudentDO validateStudentExists(Long id) {
        StudentDO student = studentMapper.selectById(id);
        if (student == null) {
            throw exception(STUDENT_NOT_EXISTS);
        }
        return student;
    }

    @Override
    public StudentDO getStudentByUserId(Long userId) {
        return studentMapper.selectByUserId(userId);
    }

    private void validateStudentNoUnique(Long id, String studentNo) {
        StudentDO student = studentMapper.selectByStudentNo(studentNo);
        if (student == null) {
            return;
        }
        // 如果 id 为空，说明是创建；否则是更新且需排除自身
        if (id == null || !id.equals(student.getId())) {
            throw exception(STUDENT_NO_EXISTS, studentNo);
        }
    }

}
