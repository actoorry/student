package vip.appap.suxin.module.campus.service;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.campus.controller.admin.vo.StudentPageReqVO;
import vip.appap.suxin.module.campus.controller.admin.vo.StudentSaveReqVO;
import vip.appap.suxin.module.campus.dal.dataobject.StudentDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;

/**
 * 学生 Service 接口
 *
 * @author 书心软件
 */
public interface StudentService {

    /**
     * 创建学生
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createStudent(@Valid StudentSaveReqVO createReqVO);

    /**
     * 更新学生
     *
     * @param updateReqVO 更新信息
     */
    void updateStudent(@Valid StudentSaveReqVO updateReqVO);

    /**
     * 删除学生
     *
     * @param id 编号
     */
    void deleteStudent(Long id);

    /**
     * 获得学生
     *
     * @param id 编号
     * @return 学生
     */
    StudentDO getStudent(Long id);

    /**
     * 获得学生分页
     *
     * @param pageReqVO 分页查询
     * @return 学生分页
     */
    PageResult<StudentDO> getStudentPage(StudentPageReqVO pageReqVO);

    /**
     * 获得学生列表
     *
     * @param ids 编号集合
     * @return 学生列表
     */
    List<StudentDO> getStudentList(Collection<Long> ids);

    /**
     * 校验学生是否存在
     *
     * @param id 编号
     * @return 学生
     */
    StudentDO validateStudentExists(Long id);

    /**
     * 根据登录账号获取学生
     *
     * @param userId 登录账号（system_users.id）
     * @return 学生，可能为 null
     */
    StudentDO getStudentByUserId(Long userId);

}
