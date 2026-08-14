package vip.appap.suxin.module.campus.service;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.campus.controller.admin.vo.ExamRecordPageReqVO;
import vip.appap.suxin.module.campus.controller.admin.vo.ExamRecordSaveReqVO;
import vip.appap.suxin.module.campus.dal.dataobject.ExamRecordDO;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

/**
 * 考试记录 Service 接口
 *
 * @author 书心软件
 */
public interface ExamRecordService {

    /**
     * 录入成绩
     *
     * @param createReqVO 成绩信息
     * @return 编号
     */
    Long createExamRecord(@Valid ExamRecordSaveReqVO createReqVO);

    /**
     * 更新成绩
     *
     * @param updateReqVO 成绩信息
     */
    void updateExamRecord(@Valid ExamRecordSaveReqVO updateReqVO);

    /**
     * 删除成绩
     *
     * @param id 编号
     */
    void deleteExamRecord(Long id);

    /**
     * 获得成绩
     *
     * @param id 编号
     * @return 成绩
     */
    ExamRecordDO getExamRecord(Long id);

    /**
     * 获得成绩分页（老师：全部；学生：传入本人 studentId 过滤）
     *
     * @param pageReqVO       分页条件
     * @param forcedStudentId 强制学生过滤（学生端传入，老师端传 null）
     * @return 成绩分页
     */
    PageResult<ExamRecordDO> getExamRecordPage(ExamRecordPageReqVO pageReqVO, Long forcedStudentId);

    /**
     * 获得指定学生当前学期的课程总分数 Map
     *
     * @param studentIds 学生编号集合
     * @param schoolYear 学年
     * @param semester   学期
     * @return Map<studentId, totalScore>
     */
    Map<Long, BigDecimal> getCurrentSemesterTotalMap(Collection<Long> studentIds, String schoolYear, Integer semester);

    /**
     * 获得每门课程及格（分数 >= 60）的去重学生数 Map
     *
     * @param courseIds 课程编号集合
     * @return Map<courseId, passCount>
     */
    Map<Long, Long> getPassCountMap(Collection<Long> courseIds);

}
