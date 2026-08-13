package vip.appap.suxin.module.system.util;

import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.framework.dict.core.DictFrameworkUtils;
import vip.appap.suxin.framework.redis.core.RedisNo;
import vip.appap.suxin.framework.security.core.LoginUser;
import vip.appap.suxin.framework.security.core.service.SecurityFrameworkService;
import vip.appap.suxin.framework.security.core.util.SecurityFrameworkUtils;
import vip.appap.suxin.framework.tenant.core.context.TenantContextHolder;
import vip.appap.suxin.module.infra.api.file.FileApi;
import vip.appap.suxin.module.system.dal.dataobject.DictDataDO;
import vip.appap.suxin.module.system.dal.dataobject.TenantDO;
import vip.appap.suxin.module.system.service.DictDataService;
import vip.appap.suxin.module.system.service.TenantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import java.time.Duration;
import java.util.List;

/**
 * 通用 Service 基类（功能服务清单）
 *
 * <p>各业务模块的 Service 可继承本类，获得以下通用能力：</p>
 * <ul>
 *   <li>Redis 全局唯一单号生成</li>
 *   <li>当前登录用户快捷获取</li>
 *   <li>当前租户快捷获取</li>
 *   <li>对象转换快捷方法</li>
 *   <li>redis 缓存快速调用</li>
 *   <li>role 权限快速调用</li>
 *   <li>dict 数据字典快速调用</li>
 *   <li>file 附件快速调用</li>
 *   <li>tenant 租户快速调用</li>
 * </ul>
 *
 * <p>注意：partner、product 等业务模块的快速调用，请直接调用对应模块的 Service 接口方法。</p>
 *
 * @author 书心软件
 */
@Slf4j
public abstract class BaseService {

    @Resource
    protected RedisNo redisNo;

    // ==================== 单号生成 ====================

    /**
     * 生成全局唯一订单号（默认前缀 "OrderSalesNo"）
     *
     * <p>格式：yyyyMMddHHmmss（14 位）+ 5 位自增流水号 = 19 位</p>
     *
     * @return 19 位唯一数字编号
     */
    protected long getOrderNo() {
        return redisNo.getOrderNo();
    }

    /**
     * 生成全局唯一编号（自定义前缀）
     *
     * @param prefix Redis key 前缀，如 "PurchaseOrderNo"、"WmsOrderNo"
     * @return 19 位唯一数字编号
     */
    protected long getNo(String prefix) {
        return redisNo.getSerialNo(prefix);
    }

    // ==================== 当前用户 ====================

    /**
     * 获取当前登录用户 ID
     *
     * @return 用户 ID，未登录返回 null
     */
    protected Long getLoginUserId() {
        return SecurityFrameworkUtils.getLoginUserId();
    }

    /**
     * 获取当前登录用户完整信息
     *
     * @return 登录用户，未登录返回 null
     */
    protected LoginUser getLoginUser() {
        return SecurityFrameworkUtils.getLoginUser();
    }

    // ==================== 当前租户 ====================

    /**
     * 获取当前租户 ID
     *
     * @return 租户 ID，不存在返回 null
     */
    protected Long getTenantId() {
        return TenantContextHolder.getTenantId();
    }

    /**
     * 获取当前租户 ID（无租户时抛异常）
     *
     * @return 租户 ID
     * @throws NullPointerException 当前上下文中无租户信息
     */
    protected Long getRequiredTenantId() {
        return TenantContextHolder.getRequiredTenantId();
    }

    // ==================== 对象转换 ====================

    /**
     * Bean 属性拷贝（类型安全）
     *
     * @param source 源对象
     * @param clazz  目标类型
     * @param <T>    目标泛型
     * @return 拷贝后的新对象
     */
    protected <T> T convert(Object source, Class<T> clazz) {
        return BeanUtils.toBean(source, clazz);
    }

    // ==================== redis 快速调用 ====================

    @Resource
    @Lazy  // 不用的 Service 不建立 redis 连接池
    protected StringRedisTemplate stringRedisTemplate;

    /**
     * 设置缓存
     */
    protected void setCache(String key, String value, Duration timeout) {
        stringRedisTemplate.opsForValue().set(key, value, timeout);
    }

    /**
     * 获取缓存
     */
    protected String getCache(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 删除缓存
     */
    protected void deleteCache(String key) {
        stringRedisTemplate.delete(key);
    }

    // ==================== 权限快速调用 ====================

    @Resource
    @Lazy
    protected SecurityFrameworkService securityFrameworkService;

    /**
     * 当前用户是否有权限
     */
    protected boolean hasPermission(String permission) {
        return securityFrameworkService.hasPermission(permission);
    }

    /**
     * 当前用户是否有角色
     */
    protected boolean hasRole(String role) {
        return securityFrameworkService.hasRole(role);
    }

    // ==================== 字典快速调用 ====================

    @Resource
    @Lazy
    protected DictDataService dictDataService;

    /**
     * 获取字典 label
     */
    protected String getDictLabel(String dictType, String value) {
        return DictFrameworkUtils.parseDictDataLabel(dictType, value);
    }

    /**
     * 获取字典数据列表
     */
    protected List<DictDataDO> getDictDataList(String dictType) {
        return dictDataService.getDictDataListByDictType(dictType);
    }

    // ==================== 文件快速调用 ====================

    @Resource
    @Lazy  // 不用的 Service 不连接对象存储
    protected FileApi fileApi;

    /**
     * 上传文件
     *
     * @return 文件路径
     */
    protected String uploadFile(byte[] content) {
        return fileApi.createFile(content);
    }

    /**
     * 获取文件预签名访问地址
     *
     * @param url 完整的文件访问地址
     * @param expirationSeconds 访问有效期，单位秒
     * @return 文件预签名地址
     */
    protected String getFileUrl(String url, Integer expirationSeconds) {
        return fileApi.presignGetUrl(url, expirationSeconds);
    }

    // ==================== 租户快速调用 ====================

    @Resource
    @Lazy
    protected TenantService tenantService;

    /**
     * 获取租户信息
     */
    protected TenantDO getTenant(Long id) {
        return tenantService.getTenant(id);
    }

}