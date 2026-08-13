package vip.appap.suxin.module.system.dal.mysql;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.system.controller.admin.vo.UserPageReqVO;
import vip.appap.suxin.module.system.dal.dataobject.AdminUserDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Collection;
import java.util.List;

@Mapper
public interface AdminUserMapper extends BaseMapperX<AdminUserDO> {

    default AdminUserDO selectByUsername(String username) {
        return selectOne(AdminUserDO::getUsername, username);
    }


    default PageResult<AdminUserDO> selectPage(UserPageReqVO reqVO, Collection<Long> deptIds, Collection<Long> userIds) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AdminUserDO>()
                .likeIfPresent(AdminUserDO::getUsername, reqVO.getUsername())
                .eqIfPresent(AdminUserDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(AdminUserDO::getCreateTime, reqVO.getCreateTime())
                .inIfPresent(AdminUserDO::getDeptId, deptIds)
                .inIfPresent(AdminUserDO::getId, userIds)
                .orderByDesc(AdminUserDO::getId));
    }

    default List<AdminUserDO> selectListByStatus(Integer status) {
        return selectList(AdminUserDO::getStatus, status);
    }

    default List<AdminUserDO> selectListByDeptIds(Collection<Long> deptIds) {
        return selectList(AdminUserDO::getDeptId, deptIds);
    }

    /**
     * 查询用户（包含已逻辑删除的记录）
     * 绕过 @TableLogic 拦截，直接查物理行
     *
     * 注意：system_users.id 为全局唯一主键（共享主键：用户 id = 客商 id = 租户 id），
     * 必须跨租户查询。若不加 {@link InterceptorIgnore}，多租户拦截器会追加 tenant_id 条件，
     * 导致在新建租户上下文内查不到其它租户已存在的账号，进而误走 INSERT 触发主键冲突
     */
    @Select("SELECT id, username, password, remark, dept_id, status, login_ip, login_date, " +
            "is_sale, is_purchase, is_mes, is_stock, deleted, tenant_id, " +
            "creator, create_time, updater, update_time " +
            "FROM system_users WHERE id = #{id}")
    @InterceptorIgnore(tenantLine = "true") // 全局主键查询：跨租户校验共享主键
    AdminUserDO selectByIdIncludeDeleted(@Param("id") Long id);

    /**
     * 复活已逻辑删除的用户
     * 绕过 @TableLogic 拦截，直接 UPDATE 物理行
     * 清除登录痕迹，防止"幽灵继承"
     * 同时将 tenant_id 置为当前租户，确保复活后的账号归属正确租户
     */
    @Update("UPDATE system_users SET " +
            "tenant_id = #{tenantId}, " +
            "username = #{username}, " +
            "password = #{password}, " +
            "remark = NULL, " +
            "dept_id = NULL, " +
            "post_ids = NULL, " +
            "status = 0, " +
            "login_ip = NULL, " +
            "login_date = NULL, " +
            "is_sale = 0, " +
            "is_purchase = 0, " +
            "is_mes = 0, " +
            "is_stock = 0, " +
            "deleted = 0 " +
            "WHERE id = #{id} AND deleted = 1")
    @InterceptorIgnore(tenantLine = "true") // 与 selectByIdIncludeDeleted 保持一致：跨租户复活共享主键账号
    int resurrectUser(@Param("id") Long id,
                      @Param("tenantId") Long tenantId,
                      @Param("username") String username,
                      @Param("password") String password);

}
