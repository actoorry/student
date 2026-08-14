package vip.appap.suxin.module.campus.api;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

/**
 * 考试记录 API 接口
 *
 * @author 书心软件
 */
public interface ExamRecordApi {

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
