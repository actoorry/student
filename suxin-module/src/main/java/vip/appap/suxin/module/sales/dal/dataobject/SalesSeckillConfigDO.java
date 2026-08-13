package vip.appap.suxin.module.sales.dal.dataobject;

import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.util.List;

/**
 * 秒杀时段 DO
 *
 * @author 书心软件
 */
@TableName(value = "sales_seckill_config", autoResultMap = true)
@KeySequence("sales_seckill_config_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesSeckillConfigDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 秒杀时段名称
     */
    private String name;
    /**
     * 开始时间点
     */
    private String startTime;
    /**
     * 结束时间点
     */
    private String endTime;
    /**
     * 秒杀轮播图
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> sliderPicUrls;
    /**
     * 状态
     *
     * 枚举 {@link CommonStatusEnum 对应的类}
     */
    private Integer status;

}
