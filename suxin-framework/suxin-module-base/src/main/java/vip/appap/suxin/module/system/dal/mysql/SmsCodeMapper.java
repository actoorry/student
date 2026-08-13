package vip.appap.suxin.module.system.dal.mysql;

import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.QueryWrapperX;
import vip.appap.suxin.module.system.dal.dataobject.SmsCodeDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;

@Mapper
public interface SmsCodeMapper extends BaseMapperX<SmsCodeDO> {

    /**
     * 获得手机号的最后一个手机验证码
     *
     * @param mobile 手机号
     * @param scene 发送场景，选填
     * @param code 验证码 选填
     * @return 手机验证码
     */
    default SmsCodeDO selectLastByMobile(String mobile, String code, Integer scene) {
        return selectOne(new QueryWrapperX<SmsCodeDO>()
                .eq("mobile", mobile)
                .eqIfPresent("scene", scene)
                .eqIfPresent("code", code)
                .orderByDesc("id")
                .limitN(1));
    }

    /**
     * 消费验证码：仅在记录存在且未被使用时更新
     * <p>
     * 通过“ID + 未使用状态”的条件更新保证并发下验证码只能被消费一次，
     * 返回更新行数，0 表示验证码已被其他请求消费（或记录不存在）。
     *
     * @param id 验证码记录编号
     * @param usedTime 使用时间
     * @param usedIp 使用 IP
     * @return 更新行数
     */
    default int updateUsed(Long id, LocalDateTime usedTime, String usedIp) {
        return update(null, new LambdaUpdateWrapper<SmsCodeDO>()
                .set(SmsCodeDO::getUsed, true)
                .set(SmsCodeDO::getUsedTime, usedTime)
                .set(SmsCodeDO::getUsedIp, usedIp)
                .eq(SmsCodeDO::getId, id)
                .eq(SmsCodeDO::getUsed, false));
    }

}
