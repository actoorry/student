package vip.appap.suxin.module.campus.api;

import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.campus.api.dto.TeacherRespDTO;
import vip.appap.suxin.module.campus.dal.dataobject.TeacherDO;
import vip.appap.suxin.module.campus.service.TeacherService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * 教师 API 实现类
 *
 * @author 书心软件
 */
@Service
public class TeacherApiImpl implements TeacherApi {

    @Resource
    private TeacherService teacherService;

    @Override
    public TeacherRespDTO getTeacher(Long id) {
        TeacherDO teacher = teacherService.getTeacher(id);
        return BeanUtils.toBean(teacher, TeacherRespDTO.class);
    }

    @Override
    public List<TeacherRespDTO> getTeacherList(Collection<Long> ids) {
        return BeanUtils.toBean(teacherService.getTeacherList(ids), TeacherRespDTO.class);
    }

    @Override
    public void validateTeacher(Long id) {
        teacherService.validateTeacherExists(id);
    }

}
