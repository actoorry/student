package vip.appap.suxin.module.campus.api;

import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.campus.api.dto.StudentRespDTO;
import vip.appap.suxin.module.campus.dal.dataobject.StudentDO;
import vip.appap.suxin.module.campus.service.StudentService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * 学生 API 实现类
 *
 * @author 书心软件
 */
@Service
public class StudentApiImpl implements StudentApi {

    @Resource
    private StudentService studentService;

    @Override
    public StudentRespDTO getStudent(Long id) {
        StudentDO student = studentService.getStudent(id);
        return BeanUtils.toBean(student, StudentRespDTO.class);
    }

    @Override
    public List<StudentRespDTO> getStudentList(Collection<Long> ids) {
        return BeanUtils.toBean(studentService.getStudentList(ids), StudentRespDTO.class);
    }

    @Override
    public void validateStudent(Long id) {
        studentService.validateStudentExists(id);
    }

    @Override
    public StudentRespDTO getStudentByUserId(Long userId) {
        StudentDO student = studentService.getStudentByUserId(userId);
        return BeanUtils.toBean(student, StudentRespDTO.class);
    }

}
