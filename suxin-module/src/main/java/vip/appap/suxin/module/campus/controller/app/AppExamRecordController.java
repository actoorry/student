package vip.appap.suxin.module.campus.controller.app;

import cn.hutool.core.collection.CollUtil;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.campus.api.CourseApi;
import vip.appap.suxin.module.campus.api.StudentApi;
import vip.appap.suxin.module.campus.api.dto.CourseRespDTO;
import vip.appap.suxin.module.campus.api.dto.StudentRespDTO;
import vip.appap.suxin.module.campus.controller.app.vo.AppExamRecordPageReqVO;
import vip.appap.suxin.module.campus.controller.app.vo.AppExamRecordRespVO;
import vip.appap.suxin.module.campus.controller.admin.vo.ExamRecordPageReqVO;
import vip.appap.suxin.module.campus.dal.dataobject.ExamRecordDO;
import vip.appap.suxin.module.campus.service.ExamRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertSet;
import static vip.appap.suxin.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 APP - 校园成绩")
@RestController
@RequestMapping("/campus/exam-record")
@Validated
public class AppExamRecordController {

    @Resource
    private ExamRecordService examRecordService;

    @Resource
    private StudentApi studentApi;

    @Resource
    private CourseApi courseApi;

    @GetMapping("/page")
    @PermitAll
    @Operation(summary = "获得我的成绩分页（学生仅查看自己的成绩）")
    public CommonResult<PageResult<AppExamRecordRespVO>> getMyExamRecordPage(@Valid AppExamRecordPageReqVO pageReqVO) {
        // 1. 根据当前登录账号反查学生身份
        Long loginUserId = getLoginUserId();
        StudentRespDTO student = studentApi.getStudentByUserId(loginUserId);
        if (student == null) {
            // 非学生身份，返回空
            return success(PageResult.empty());
        }
        // 2. 构造管理端分页条件，强制只查本人
        ExamRecordPageReqVO examPageReqVO = new ExamRecordPageReqVO()
                .setCourseId(pageReqVO.getCourseId())
                .setSchoolYear(pageReqVO.getSchoolYear())
                .setSemester(pageReqVO.getSemester());
        examPageReqVO.setPageNo(pageReqVO.getPageNo());
        examPageReqVO.setPageSize(pageReqVO.getPageSize());
        PageResult<ExamRecordDO> pageResult = examRecordService.getExamRecordPage(examPageReqVO, student.getId());
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(BeanUtils.toBean(pageResult, AppExamRecordRespVO.class));
        }
        // 3. 关联课程名称
        Map<Long, CourseRespDTO> courseMap = courseApi.getCourseMap(
                convertSet(pageResult.getList(), ExamRecordDO::getCourseId));
        // 4. 拼装 VO
        return success(BeanUtils.toBean(pageResult, AppExamRecordRespVO.class, vo -> {
            CourseRespDTO course = courseMap.get(vo.getCourseId());
            if (course != null) {
                vo.setCourseName(course.getCourseName());
            }
        }));
    }

}
