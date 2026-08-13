package vip.appap.suxin.module.ai.dal.mysql.mindmap;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.ai.controller.admin.mindmap.vo.AiMindMapPageReqVO;
import vip.appap.suxin.module.ai.dal.dataobject.mindmap.AiMindMapDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 思维导图 Mapper
 *
 * @author xiaoxin
 */
@Mapper
public interface AiMindMapMapper extends BaseMapperX<AiMindMapDO> {

    default PageResult<AiMindMapDO> selectPage(AiMindMapPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AiMindMapDO>()
                .eqIfPresent(AiMindMapDO::getUserId, reqVO.getUserId())
                .eqIfPresent(AiMindMapDO::getPrompt, reqVO.getPrompt())
                .betweenIfPresent(AiMindMapDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(AiMindMapDO::getId));
    }

}
