package vip.appap.suxin.module.campus.controller.admin;

import cn.hutool.core.collection.CollUtil;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.campus.api.ExamRecordApi;
import vip.appap.suxin.module.campus.controller.admin.vo.StudentPageReqVO;
import vip.appap.suxin.module.campus.controller.admin.vo.StudentRespVO;
import vip.appap.suxin.module.campus.controller.admin.vo.StudentSaveReqVO;
import vip.appap.suxin.module.campus.dal.dataobject.StudentDO;
import vip.appap.suxin.module.campus.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - 校园学生")
@RestController
@RequestMapping("/campus/student")
@Validated
public class StudentController {

    @Resource
    private StudentService studentService;

    @Resource
    private ExamRecordApi examRecordApi;

    @PostMapping("/create")
    @Operation(summary = "创建学生")
    @PreAuthorize("@ss.hasPermission('campus:student:create')")
    public CommonResult<Long> createStudent(@Valid @RequestBody StudentSaveReqVO createReqVO) {
        return success(studentService.createStudent(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新学生")
    @PreAuthorize("@ss.hasPermission('campus:student:update')")
    public CommonResult<Boolean> updateStudent(@Valid @RequestBody StudentSaveReqVO updateReqVO) {
        studentService.updateStudent(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除学生")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('campus:student:delete')")
    public CommonResult<Boolean> deleteStudent(@RequestParam("id") Long id) {
        studentService.deleteStudent(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得学生")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('campus:student:query')")
    public CommonResult<StudentRespVO> getStudent(@RequestParam("id") Long id) {
        StudentDO student = studentService.getStudent(id);
        return success(BeanUtils.toBean(student, StudentRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得学生分页（含当前学期总分数）")
    @PreAuthorize("@ss.hasPermission('campus:student:query')")
    public CommonResult<PageResult<StudentRespVO>> getStudentPage(@Valid StudentPageReqVO pageReqVO) {
        // 1. 查询学生分页
        PageResult<StudentDO> pageResult = studentService.getStudentPage(pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(BeanUtils.toBean(pageResult, StudentRespVO.class));
        }
        // 2. 动态统计当前学期总分（仅在请求携带学年+学期时计算）
        Map<Long, BigDecimal> totalMap = null;
        if (pageReqVO.getSchoolYear() != null && pageReqVO.getSemester() != null) {
            totalMap = examRecordApi.getCurrentSemesterTotalMap(
                    convertSet(pageResult.getList(), StudentDO::getId),
                    pageReqVO.getSchoolYear(), pageReqVO.getSemester());
        }
        // 3. 拼装 VO
        final Map<Long, BigDecimal> finalTotalMap = totalMap;
        return success(BeanUtils.toBean(pageResult, StudentRespVO.class, vo -> {
            if (finalTotalMap != null) {
                vo.setCurrentSemesterTotal(finalTotalMap.getOrDefault(vo.getId(), BigDecimal.ZERO));
            }
        }));
    }

}
