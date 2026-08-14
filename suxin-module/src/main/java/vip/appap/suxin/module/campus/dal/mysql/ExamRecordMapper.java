package vip.appap.suxin.module.campus.dal.mysql;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.campus.controller.admin.vo.ExamRecordPageReqVO;
import vip.appap.suxin.module.campus.dal.dataobject.ExamRecordDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 考试记录 Mapper
 *
 * @author 书心软件
 */
@Mapper
public interface ExamRecordMapper extends BaseMapperX<ExamRecordDO> {

    /**
     * 分页查询考试记录
     *
     * @param pageReqVO      分页条件
     * @param forcedStudentId 强制过滤的学生编号（学生端传入本人 id，老师端传 null）
     */
    default PageResult<ExamRecordDO> selectPage(ExamRecordPageReqVO pageReqVO, Long forcedStudentId) {
        return selectPage(pageReqVO, new LambdaQueryWrapperX<ExamRecordDO>()
                .eq(forcedStudentId != null, ExamRecordDO::getStudentId, forcedStudentId)
                .eqIfPresent(ExamRecordDO::getStudentId, pageReqVO.getStudentId())
                .eqIfPresent(ExamRecordDO::getCourseId, pageReqVO.getCourseId())
                .eqIfPresent(ExamRecordDO::getSchoolYear, pageReqVO.getSchoolYear())
                .eqIfPresent(ExamRecordDO::getSemester, pageReqVO.getSemester())
                .orderByDesc(ExamRecordDO::getId));
    }

    /**
     * 查询指定学生当前学期的课程总分数
     *
     * @param studentIds 学生编号集合
     * @param schoolYear 学年
     * @param semester   学期
     * @return Map<studentId, totalScore>
     */
    @Select("<script>" +
            "SELECT student_id AS studentId, COALESCE(SUM(score), 0) AS totalScore " +
            "FROM campus_exam_record " +
            "WHERE deleted = 0 " +
            "AND school_year = #{schoolYear} " +
            "AND semester = #{semester} " +
            "AND student_id IN " +
            "<foreach collection='studentIds' item='id' open='(' separator=',' close=')'>#{id}</foreach> " +
            "GROUP BY student_id" +
            "</script>")
    List<Map<String, Object>> selectTotalScoreByStudentIds(@Param("studentIds") Collection<Long> studentIds,
                                                           @Param("schoolYear") String schoolYear,
                                                           @Param("semester") Integer semester);

    /**
     * 查询每门课程及格（分数 >= 60）的去重学生数，跨学期累计
     *
     * @param courseIds 课程编号集合
     * @return Map<courseId, passCount>
     */
    @Select("<script>" +
            "SELECT course_id AS courseId, COUNT(DISTINCT CASE WHEN score >= 60 THEN student_id END) AS passCount " +
            "FROM campus_exam_record " +
            "WHERE deleted = 0 " +
            "AND course_id IN " +
            "<foreach collection='courseIds' item='id' open='(' separator=',' close=')'>#{id}</foreach> " +
            "GROUP BY course_id" +
            "</script>")
    List<Map<String, Object>> selectPassCountByCourseIds(@Param("courseIds") Collection<Long> courseIds);

    /**
     * 查询唯一成绩记录（用于去重校验）
     */
    default ExamRecordDO selectByUniqueKey(Long studentId, Long courseId, String schoolYear, Integer semester) {
        return selectOne(new LambdaQueryWrapperX<ExamRecordDO>()
                .eq(ExamRecordDO::getStudentId, studentId)
                .eq(ExamRecordDO::getCourseId, courseId)
                .eq(ExamRecordDO::getSchoolYear, schoolYear)
                .eq(ExamRecordDO::getSemester, semester));
    }

}
