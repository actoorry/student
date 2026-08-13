package vip.appap.suxin.module.hr.service.positionchange;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.hr.controller.admin.positionchange.vo.*;
import vip.appap.suxin.module.hr.dal.dataobject.employee.EmployeeDO;
import vip.appap.suxin.module.hr.dal.dataobject.positionchange.PositionChangeDO;
import vip.appap.suxin.module.hr.dal.mysql.employee.EmployeeMapper;
import vip.appap.suxin.module.hr.dal.mysql.positionchange.PositionChangeMapper;
import vip.appap.suxin.module.hr.service.employee.EmployeePostSyncService;
import vip.appap.suxin.module.system.dal.dataobject.PostDO;
import vip.appap.suxin.module.system.service.PostService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.module.hr.enums.ErrorCodeConstants.*;

@Service
@Validated
public class PositionChangeServiceImpl implements PositionChangeService {

    @Resource
    private PositionChangeMapper positionChangeMapper;
    @Resource
    private EmployeeMapper employeeMapper;
    @Resource
    private PostService postService;
    @Resource
    private EmployeePostSyncService employeePostSyncService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPositionChange(PositionChangeSaveReqVO createReqVO) {
        PositionChangeDO change = buildPositionChange(createReqVO);
        positionChangeMapper.insert(change);
        syncEmployeeIfNeeded(change);
        return change.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePositionChange(PositionChangeSaveReqVO updateReqVO) {
        validatePositionChangeExists(updateReqVO.getId());
        PositionChangeDO change = buildPositionChange(updateReqVO);
        positionChangeMapper.updateById(change);
        syncEmployeeIfNeeded(change);
    }

    @Override
    public void deletePositionChange(Long id) {
        validatePositionChangeExists(id);
        positionChangeMapper.deleteById(id);
    }

    @Override
    public PositionChangeRespVO getPositionChange(Long id) {
        PositionChangeRespVO resp = positionChangeMapper.selectByIdJoin(id);
        if (resp == null) {
            throw exception(POSITION_CHANGE_NOT_EXISTS);
        }
        return resp;
    }

    @Override
    public PageResult<PositionChangeRespVO> getPositionChangePage(PositionChangePageReqVO pageReqVO) {
        return positionChangeMapper.selectPage(pageReqVO);
    }

    @Override
    public List<PositionTimelineRespVO> getPositionTimeline(Long partnerId) {
        return positionChangeMapper.selectTimelineByPartnerId(partnerId);
    }

    private PositionChangeDO buildPositionChange(PositionChangeSaveReqVO reqVO) {
        EmployeeDO employee = validateEmployeeExists(reqVO.getPartnerId());
        // 原岗位默认带入员工当前 postId
        if (reqVO.getFromPostId() == null) {
            reqVO.setFromPostId(employee.getPostId());
        }
        // 校验系统岗位并读取名称写入快照
        List<Long> postIds = new java.util.ArrayList<>();
        if (reqVO.getFromPostId() != null) {
            postIds.add(reqVO.getFromPostId());
        }
        if (reqVO.getToPostId() != null) {
            postIds.add(reqVO.getToPostId());
        }
        if (!postIds.isEmpty()) {
            postService.validatePostList(postIds);
        }
        if (reqVO.getFromPostId() != null) {
            PostDO fromPost = postService.getPost(reqVO.getFromPostId());
            reqVO.setFromPost(fromPost != null ? fromPost.getName() : null);
        }
        if (reqVO.getToPostId() != null) {
            PostDO toPost = postService.getPost(reqVO.getToPostId());
            reqVO.setToPost(toPost != null ? toPost.getName() : null);
        }
        PositionChangeDO change = BeanUtils.toBean(reqVO, PositionChangeDO.class);
        change.setEmployeeNo(employee.getEmployeeNo());
        if (change.getSyncEmployee() == null) {
            change.setSyncEmployee(0);
        }
        if (change.getFromDept() == null) {
            change.setFromDept(employee.getDept());
        }
        return change;
    }

    private void syncEmployeeIfNeeded(PositionChangeDO change) {
        if (change.getSyncEmployee() == null || change.getSyncEmployee() != 1) {
            return;
        }
        EmployeeDO employee = employeeMapper.selectProfileByPartnerId(change.getPartnerId());
        if (employee == null) {
            return;
        }
        EmployeeDO update = new EmployeeDO();
        update.setId(employee.getId());
        if (change.getToDept() != null) {
            update.setDept(change.getToDept());
        }
        if (change.getToPostId() != null) {
            update.setPostId(change.getToPostId());
            update.setPosition(change.getToPost());
        }
        employeeMapper.updateById(update);
        employeePostSyncService.syncUserPostFromEmployee(change.getPartnerId(), update.getPostId());
    }

    private PositionChangeDO validatePositionChangeExists(Long id) {
        PositionChangeDO change = positionChangeMapper.selectById(id);
        if (change == null) {
            throw exception(POSITION_CHANGE_NOT_EXISTS);
        }
        return change;
    }

    private EmployeeDO validateEmployeeExists(Long partnerId) {
        EmployeeDO employee = employeeMapper.selectProfileByPartnerId(partnerId);
        if (employee == null) {
            throw exception(POSITION_CHANGE_EMPLOYEE_NOT_EXISTS);
        }
        return employee;
    }

}
