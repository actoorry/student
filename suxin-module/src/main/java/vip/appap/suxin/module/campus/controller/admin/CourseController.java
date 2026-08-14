package vip.appap.suxin.module.campus.controller.admin;

import cn.hutool.core.collection.CollUtil;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.campus.api.ExamRecordApi;
import vip.appap.suxin.module.campus.api.TeacherApi;
import vip.appap.suxin.module.campus.api.dto.TeacherRespDTO;
import vip.appap.suxin.module.campus.controller.admin.vo.CoursePageReqVO;
import vip.appap.suxin.module.campus.controller.admin.vo.CourseRespVO;
import vip.appap.suxin.module.campus.controller.admin.vo.CourseSaveReqVO;
import vip.appap.suxin.module.campus.dal.dataobject.CourseDO;
import vip.appap.suxin.module.campus.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - 校园课程")
@RestController
@RequestMapping("/campus/course")
@Validated
public class CourseController {

    @Resource
    private CourseService courseService;

    @Resource
    private ExamRecordApi examRecordApi;

    @Resource
    private TeacherApi teacherApi;

    @PostMapping("/create")
    @Operation(summary = "创建课程")
    @PreAuthorize("@ss.hasPermission('campus:course:create')")
    public CommonResult<Long> createCourse(@Valid @RequestBody CourseSaveReqVO createReqVO) {
        return success(courseService.createCourse(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新课程")
    @PreAuthorize("@ss.hasPermission('campus:course:update')")
    public CommonResult<Boolean> updateCourse(@Valid @RequestBody CourseSaveReqVO updateReqVO) {
        courseService.updateCourse(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除课程")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('campus:course:delete')")
    public CommonResult<Boolean> deleteCourse(@RequestParam("id") Long id) {
        courseService.deleteCourse(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得课程")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('campus:course:query')")
    public CommonResult<CourseRespVO> getCourse(@RequestParam("id") Long id) {
        CourseDO course = courseService.getCourse(id);
        if (course == null) {
            return success(null);
        }
        CourseRespVO respVO = BeanUtils.toBean(course, CourseRespVO.class);
        // 填充教师姓名
        if (course.getTeacherId() != null) {
            TeacherRespDTO teacher = teacherApi.getTeacher(course.getTeacherId());
            if (teacher != null) {
                respVO.setTeacherName(teacher.getName());
            }
        }
        return success(respVO);
    }

    @GetMapping("/page")
    @Operation(summary = "获得课程分页（含及格总人数）")
    @PreAuthorize("@ss.hasPermission('campus:course:query')")
    public CommonResult<PageResult<CourseRespVO>> getCoursePage(@Valid CoursePageReqVO pageReqVO) {
        // 1. 查询课程分页
        PageResult<CourseDO> pageResult = courseService.getCoursePage(pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(BeanUtils.toBean(pageResult, CourseRespVO.class));
        }
        // 2. 动态统计及格人数
        Map<Long, Long> passCountMap = examRecordApi.getPassCountMap(
                convertSet(pageResult.getList(), CourseDO::getId));
        // 3. 教师姓名 Map
        Map<Long, TeacherRespDTO> teacherMap = teacherApi.getTeacherMap(
                convertSet(pageResult.getList(), CourseDO::getTeacherId));
        // 4. 拼装 VO
        return success(BeanUtils.toBean(pageResult, CourseRespVO.class, vo -> {
            vo.setPassCount(passCountMap.getOrDefault(vo.getId(), 0L));
            TeacherRespDTO teacher = teacherMap.get(vo.getTeacherId());
            if (teacher != null) {
                vo.setTeacherName(teacher.getName());
            }
        }));
    }

    @PostMapping("/select")
    @Operation(summary = "学生选课")
    @Parameter(name = "studentId", description = "学生编号", required = true)
    @Parameter(name = "courseId", description = "课程编号", required = true)
    @PreAuthorize("@ss.hasPermission('campus:course:select')")
    public CommonResult<Boolean> selectCourse(@RequestParam("studentId") Long studentId,
                                              @RequestParam("courseId") Long courseId) {
        courseService.selectCourse(studentId, courseId);
        return success(true);
    }

}
