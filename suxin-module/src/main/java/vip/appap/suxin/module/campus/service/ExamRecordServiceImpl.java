package vip.appap.suxin.module.campus.service;

import cn.hutool.core.collection.CollUtil;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.campus.api.CourseApi;
import vip.appap.suxin.module.campus.api.StudentApi;
import vip.appap.suxin.module.campus.controller.admin.vo.ExamRecordPageReqVO;
import vip.appap.suxin.module.campus.controller.admin.vo.ExamRecordSaveReqVO;
import vip.appap.suxin.module.campus.dal.dataobject.ExamRecordDO;
import vip.appap.suxin.module.campus.dal.mysql.ExamRecordMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.module.campus.enums.ErrorCodeConstants.EXAM_RECORD_EXISTS;
import static vip.appap.suxin.module.campus.enums.ErrorCodeConstants.EXAM_RECORD_NOT_EXISTS;

/**
 * 考试记录 Service 实现类
 *
 * @author 书心软件
 */
@Service
@Validated
public class ExamRecordServiceImpl implements ExamRecordService {

    @Resource
    private ExamRecordMapper examRecordMapper;

    @Resource
    private StudentApi studentApi;

    @Resource
    private CourseApi courseApi;

    @Override
    public Long createExamRecord(ExamRecordSaveReqVO createReqVO) {
        // 校验学生与课程存在
        studentApi.validateStudent(createReqVO.getStudentId());
        courseApi.validateCourse(createReqVO.getCourseId());
        // 校验唯一（该学生该课程该学期不能重复录入）
        validateUnique(createReqVO.getStudentId(), createReqVO.getCourseId(),
                createReqVO.getSchoolYear(), createReqVO.getSemester(), null);
        // 插入
        ExamRecordDO examRecord = BeanUtils.toBean(createReqVO, ExamRecordDO.class);
        examRecordMapper.insert(examRecord);
        return examRecord.getId();
    }

    @Override
    public void updateExamRecord(ExamRecordSaveReqVO updateReqVO) {
        // 校验存在
        validateExamRecordExists(updateReqVO.getId());
        // 校验学生与课程存在
        studentApi.validateStudent(updateReqVO.getStudentId());
        courseApi.validateCourse(updateReqVO.getCourseId());
        // 校验唯一（排除自身）
        validateUnique(updateReqVO.getStudentId(), updateReqVO.getCourseId(),
                updateReqVO.getSchoolYear(), updateReqVO.getSemester(), updateReqVO.getId());
        // 更新
        ExamRecordDO updateObj = BeanUtils.toBean(updateReqVO, ExamRecordDO.class);
        examRecordMapper.updateById(updateObj);
    }

    @Override
    public void deleteExamRecord(Long id) {
        // 校验存在
        validateExamRecordExists(id);
        // 删除
        examRecordMapper.deleteById(id);
    }

    @Override
    public ExamRecordDO getExamRecord(Long id) {
        return examRecordMapper.selectById(id);
    }

    @Override
    public PageResult<ExamRecordDO> getExamRecordPage(ExamRecordPageReqVO pageReqVO, Long forcedStudentId) {
        return examRecordMapper.selectPage(pageReqVO, forcedStudentId);
    }

    @Override
    public Map<Long, BigDecimal> getCurrentSemesterTotalMap(Collection<Long> studentIds, String schoolYear, Integer semester) {
        Map<Long, BigDecimal> result = new HashMap<>();
        if (CollUtil.isEmpty(studentIds)) {
            return result;
        }
        List<Map<String, Object>> rows = examRecordMapper.selectTotalScoreByStudentIds(studentIds, schoolYear, semester);
        for (Map<String, Object> row : rows) {
            Long studentId = toLong(row.get("studentId"));
            BigDecimal totalScore = row.get("totalScore") == null ? BigDecimal.ZERO : new BigDecimal(row.get("totalScore").toString());
            result.put(studentId, totalScore);
        }
        return result;
    }

    @Override
    public Map<Long, Long> getPassCountMap(Collection<Long> courseIds) {
        Map<Long, Long> result = new HashMap<>();
        if (CollUtil.isEmpty(courseIds)) {
            return result;
        }
        List<Map<String, Object>> rows = examRecordMapper.selectPassCountByCourseIds(courseIds);
        for (Map<String, Object> row : rows) {
            Long courseId = toLong(row.get("courseId"));
            Long passCount = row.get("passCount") == null ? 0L : Long.parseLong(row.get("passCount").toString());
            result.put(courseId, passCount);
        }
        return result;
    }

    private void validateUnique(Long studentId, Long courseId, String schoolYear, Integer semester, Long excludeId) {
        ExamRecordDO exist = examRecordMapper.selectByUniqueKey(studentId, courseId, schoolYear, semester);
        if (exist == null) {
            return;
        }
        if (excludeId == null || !excludeId.equals(exist.getId())) {
            throw exception(EXAM_RECORD_EXISTS);
        }
    }

    private ExamRecordDO validateExamRecordExists(Long id) {
        ExamRecordDO examRecord = examRecordMapper.selectById(id);
        if (examRecord == null) {
            throw exception(EXAM_RECORD_NOT_EXISTS);
        }
        return examRecord;
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.parseLong(value.toString());
    }

}
