package vip.appap.suxin.module.marriage.dal.mysql;

import vip.appap.suxin.framework.common.pojo.PageParam;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.marriage.dal.dataobject.PartnerMomentDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface PartnerMomentMapper extends BaseMapperX<PartnerMomentDO> {

    /** 公开动态分页：仅已发布且公开的内容可由他人查看。 */
    default PageResult<PartnerMomentDO> selectPublicPage(PageParam reqVO, Collection<Long> partnerIds) {
        LambdaQueryWrapperX<PartnerMomentDO> query = new LambdaQueryWrapperX<PartnerMomentDO>()
                .eq(PartnerMomentDO::getStatus, 1)
                .eq(PartnerMomentDO::getVisibility, 1)
                .orderByDesc(PartnerMomentDO::getPublishTime)
                .orderByDesc(PartnerMomentDO::getId);
        if (partnerIds != null) {
            if (partnerIds.isEmpty()) {
                query.eq(PartnerMomentDO::getId, -1L);
            } else {
                query.in(PartnerMomentDO::getPartnerId, partnerIds);
            }
        }
        return selectPage(reqVO, query);
    }

    /** 拥有者动态分页：身份由服务端登录上下文决定，不复用公开可见性条件。 */
    default PageResult<PartnerMomentDO> selectOwnerPage(PageParam reqVO, Long partnerId) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PartnerMomentDO>()
                .eq(PartnerMomentDO::getPartnerId, partnerId)
                .eq(PartnerMomentDO::getStatus, 1)
                .orderByDesc(PartnerMomentDO::getPublishTime)
                .orderByDesc(PartnerMomentDO::getId));
    }

    default PartnerMomentDO selectLatestByPartnerId(Long partnerId) {
        if (partnerId == null) {
            return null;
        }
        return selectOne(new LambdaQueryWrapperX<PartnerMomentDO>()
                .eq(PartnerMomentDO::getPartnerId, partnerId)
                .eq(PartnerMomentDO::getStatus, 1)
                .eq(PartnerMomentDO::getVisibility, 1)
                .orderByDesc(PartnerMomentDO::getPublishTime)
                .orderByDesc(PartnerMomentDO::getId)
                .last("LIMIT 1"));
    }

    default Map<Long, PartnerMomentDO> selectLatestByPartnerIds(Collection<Long> partnerIds) {
        if (partnerIds == null || partnerIds.isEmpty()) {
            return java.util.Map.of();
        }
        // MySQL 8 在数据库内按人物分组排名，避免把人物的全部历史动态拉回 JVM。
        List<PartnerMomentDO> moments = selectLatestPublicByPartnerIds(partnerIds);
        Map<Long, PartnerMomentDO> result = new java.util.LinkedHashMap<>();
        for (PartnerMomentDO moment : moments) {
            result.put(moment.getPartnerId(), moment);
        }
        return result;
    }

    @Select("""
            <script>
            SELECT latest.* FROM (
                SELECT pm.*, ROW_NUMBER() OVER (
                    PARTITION BY pm.partner_id ORDER BY pm.publish_time DESC, pm.id DESC
                ) AS latest_rank
                FROM partner_moment pm
                WHERE pm.deleted = 0
                  AND pm.status = 1
                  AND pm.visibility = 1
                  AND pm.partner_id IN
                  <foreach collection="partnerIds" item="partnerId" open="(" separator="," close=")">
                    #{partnerId}
                  </foreach>
            ) latest
            WHERE latest.latest_rank = 1
            </script>
            """)
    List<PartnerMomentDO> selectLatestPublicByPartnerIds(@Param("partnerIds") Collection<Long> partnerIds);

    default List<PartnerMomentDO> selectListByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return selectByIds(ids);
    }

    default int updateLikeCount(Long id, int delta) {
        return update(new LambdaUpdateWrapper<PartnerMomentDO>()
                .eq(PartnerMomentDO::getId, id)
                .setSql("like_count = GREATEST(like_count + " + delta + ", 0)"));
    }

    default int updateCommentCount(Long id, int delta) {
        return update(new LambdaUpdateWrapper<PartnerMomentDO>()
                .eq(PartnerMomentDO::getId, id)
                .setSql("comment_count = GREATEST(comment_count + " + delta + ", 0)"));
    }

}
