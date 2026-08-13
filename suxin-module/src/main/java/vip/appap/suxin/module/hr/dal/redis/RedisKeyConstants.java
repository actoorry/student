package vip.appap.suxin.module.hr.dal.redis;

/**
 * HR Redis Key 枚举类
 */
public interface RedisKeyConstants {

    /**
     * 劳动合同编号自增
     * KEY 格式：hr:contract:no:{yyyyMMdd}
     */
    String CONTRACT_NO = "hr:contract:no:";

}
