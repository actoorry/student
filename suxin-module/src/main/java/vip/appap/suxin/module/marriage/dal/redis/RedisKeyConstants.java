package vip.appap.suxin.module.marriage.dal.redis;

/**
 * Marriage Redis Key 枚举类
 *
 * @author 书心软件
 */
public interface RedisKeyConstants {

    /**
     * 首页推荐会员分页的缓存
     * <p>
     * KEY 格式：marriage_recommend_page:{sex}:{pageNo}:{pageSize}
     * VALUE 数据类型：String 推荐会员分页数据
     */
    String MARRIAGE_RECOMMEND_PAGE = "marriage_recommend_page";

}
