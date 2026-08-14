package vip.appap.suxin.module.campus.enums;

import vip.appap.suxin.framework.common.exception.ErrorCode;

/**
 * 校园管理错误码枚举类
 * <p>
 * campus 系统，使用 1-060-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== 学生管理 1-060-000-000 ==========
    ErrorCode STUDENT_NOT_EXISTS = new ErrorCode(1_060_000_000, "学生不存在");
    ErrorCode STUDENT_NO_EXISTS = new ErrorCode(1_060_000_001, "学号【{}】已存在");

    // ========== 教师管理 1-060-001-000 ==========
    ErrorCode TEACHER_NOT_EXISTS = new ErrorCode(1_060_001_000, "教师不存在");
    ErrorCode TEACHER_NO_EXISTS = new ErrorCode(1_060_001_001, "工号【{}】已存在");

    // ========== 课程管理 1-060-002-000 ==========
    ErrorCode COURSE_NOT_EXISTS = new ErrorCode(1_060_002_000, "课程不存在");
    ErrorCode COURSE_CODE_EXISTS = new ErrorCode(1_060_002_001, "课程编号【{}】已存在");

    // ========== 选课管理 1-060-003-000 ==========
    ErrorCode STUDENT_COURSE_EXISTS = new ErrorCode(1_060_003_000, "该学生已选修此课程，请勿重复选课");

    // ========== 考试记录 1-060-004-000 ==========
    ErrorCode EXAM_RECORD_NOT_EXISTS = new ErrorCode(1_060_004_000, "考试记录不存在");
    ErrorCode EXAM_RECORD_EXISTS = new ErrorCode(1_060_004_001, "该学生该课程该学期成绩已存在，请勿重复录入");

}
