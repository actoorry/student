package vip.appap.suxin.module.campus.controller.admin;

import cn.hutool.core.collection.CollUtil;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.campus.api.CourseApi;
import vip.appap.suxin.module.campus.api.StudentApi;
import vip.appap.suxin.module.campus.api.dto.CourseRespDTO;
import vip.appap.suxin.module.campus.api.dto.StudentRespDTO;
import vip.appap.suxin.module.campus.controller.admin.vo.ExamRecordPageReqVO;
import vip.appap.suxin.module.campus.controller.admin.vo.ExamRecordRespVO;
import vip.appap.suxin.module.campus.controller.admin.vo.ExamRecordSaveReqVO;
import vip.appap.suxin.module.campus.dal.dataobject.ExamRecordDO;
import vip.appap.suxin.module.campus.service.ExamRecordService;
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

@Tag(name = "管理后台 - 校园考试记录")
@RestController
@RequestMapping("/campus/exam-record")
@Validated
public class ExamRecordController {

    @Resource
    private ExamRecordService examRecordService;

    @Resource
    private StudentApi studentApi;

    @Resource
    private CourseApi courseApi;

    @PostMapping("/create")
    @Operation(summary = "录入成绩")
    @PreAuthorize("@ss.hasPermission('campus:exam-record:create')")
    public CommonResult<Long> createExamRecord(@Valid @RequestBody ExamRecordSaveReqVO createReqVO) {
        return success(examRecordService.createExamRecord(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新成绩")
    @PreAuthorize("@ss.hasPermission('campus:exam-record:update')")
    public CommonResult<Boolean> updateExamRecord(@Valid @RequestBody ExamRecordSaveReqVO updateReqVO) {
        examRecordService.updateExamRecord(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除成绩")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('campus:exam-record:delete')")
    public CommonResult<Boolean> deleteExamRecord(@RequestParam("id") Long id) {
        examRecordService.deleteExamRecord(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得成绩")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('campus:exam-record:query')")
    public CommonResult<ExamRecordRespVO> getExamRecord(@RequestParam("id") Long id) {
        ExamRecordDO examRecord = examRecordService.getExamRecord(id);
        if (examRecord == null) {
            return success(null);
        }
        ExamRecordRespVO respVO = BeanUtils.toBean(examRecord, ExamRecordRespVO.class);
        // 填充学生姓名
        StudentRespDTO student = studentApi.getStudent(examRecord.getStudentId());
        if (student != null) {
            respVO.setStudentName(student.getName());
        }
        // 填充课程名称
        CourseRespDTO course = courseApi.getCourse(examRecord.getCourseId());
        if (course != null) {
            respVO.setCourseName(course.getCourseName());
        }
        return success(respVO);
    }

    @GetMapping("/page")
    @Operation(summary = "获得成绩分页（老师查看全部）")
    @PreAuthorize("@ss.hasPermission('campus:exam-record:query')")
    public CommonResult<PageResult<ExamRecordRespVO>> getExamRecordPage(@Valid ExamRecordPageReqVO pageReqVO) {
        // 老师查全部，forcedStudentId 传 null
        PageResult<ExamRecordDO> pageResult = examRecordService.getExamRecordPage(pageReqVO, null);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(BeanUtils.toBean(pageResult, ExamRecordRespVO.class));
        }
        // 关联学生姓名 + 课程名称（跨服务 Api 契约）
        Map<Long, StudentRespDTO> studentMap = studentApi.getStudentMap(
                convertSet(pageResult.getList(), ExamRecordDO::getStudentId));
        Map<Long, CourseRespDTO> courseMap = courseApi.getCourseMap(
                convertSet(pageResult.getList(), ExamRecordDO::getCourseId));
        // 拼装 VO
        return success(BeanUtils.toBean(pageResult, ExamRecordRespVO.class, vo -> {
            StudentRespDTO student = studentMap.get(vo.getStudentId());
            if (student != null) {
                vo.setStudentName(student.getName());
            }
            CourseRespDTO course = courseMap.get(vo.getCourseId());
            if (course != null) {
                vo.setCourseName(course.getCourseName());
            }
        }));
    }

}
