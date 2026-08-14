package vip.appap.suxin.module.campus.api;

import vip.appap.suxin.module.campus.service.ExamRecordService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

/**
 * 考试记录 API 实现类
 *
 * @author 书心软件
 */
@Service
public class ExamRecordApiImpl implements ExamRecordApi {

    @Resource
    private ExamRecordService examRecordService;

    @Override
    public Map<Long, BigDecimal> getCurrentSemesterTotalMap(Collection<Long> studentIds, String schoolYear, Integer semester) {
        return examRecordService.getCurrentSemesterTotalMap(studentIds, schoolYear, semester);
    }

    @Override
    public Map<Long, Long> getPassCountMap(Collection<Long> courseIds) {
        return examRecordService.getPassCountMap(courseIds);
    }

}
