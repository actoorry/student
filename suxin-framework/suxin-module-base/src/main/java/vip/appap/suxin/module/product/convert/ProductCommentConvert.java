package vip.appap.suxin.module.product.convert;

import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.partner.api.dto.PartnerRespDTO;
import vip.appap.suxin.module.product.api.dto.ProductCommentCreateReqDTO;
import vip.appap.suxin.module.product.controller.admin.vo.ProductCommentCreateReqVO;
import vip.appap.suxin.module.product.dal.dataobject.ProductCommentDO;
import vip.appap.suxin.module.product.dal.dataobject.ProductSkuDO;
import vip.appap.suxin.module.product.dal.dataobject.ProductSpuDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 商品评价 Convert
 *
 * @author wangzhs
 */
@Mapper
public interface ProductCommentConvert {

    ProductCommentConvert INSTANCE = Mappers.getMapper(ProductCommentConvert.class);

    default ProductCommentDO convert(ProductCommentCreateReqDTO createReqDTO,
                                     ProductSpuDO spu, ProductSkuDO sku, PartnerRespDTO user) {
        ProductCommentDO comment = BeanUtils.toBean(createReqDTO, ProductCommentDO.class)
                .setScores(convertScores(createReqDTO.getDescriptionScores(), createReqDTO.getBenefitScores()));
        if (user != null) {
            comment.setUserId(user.getId()).setUserNickname(user.getNickname()).setUserAvatar(user.getAvatar());
        }
        if (spu != null) {
            comment.setSpuId(spu.getId()).setSpuName(spu.getName());
        }
        if (sku != null) {
            comment.setSkuPicUrl(sku.getPicUrl()).setSkuProperties(sku.getProperties());
        }
        return comment;
    }

    default ProductCommentDO convert(ProductCommentCreateReqVO createReq, ProductSpuDO spu, ProductSkuDO sku) {
        ProductCommentDO comment = BeanUtils.toBean(createReq, ProductCommentDO.class)
                .setVisible(true).setUserId(0L).setAnonymous(false)
                .setScores(convertScores(createReq.getDescriptionScores(), createReq.getBenefitScores()));
        if (spu != null) {
            comment.setSpuId(spu.getId()).setSpuName(spu.getName());
        }
        if (sku != null) {
            comment.setSkuPicUrl(sku.getPicUrl()).setSkuProperties(sku.getProperties());
        }
        return comment;
    }

    default Integer convertScores(Integer descriptionScores, Integer benefitScores) {
        // 计算评价最终综合评分 最终星数 = （商品评星 + 服务评星） / 2
        BigDecimal sumScore = new BigDecimal(descriptionScores + benefitScores);
        BigDecimal divide = sumScore.divide(BigDecimal.valueOf(2L), 0, RoundingMode.DOWN);
        return divide.intValue();
    }

}
