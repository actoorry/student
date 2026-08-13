package vip.appap.suxin.module.infra.convert.file;

import vip.appap.suxin.module.infra.controller.admin.file.vo.config.FileConfigSaveReqVO;
import vip.appap.suxin.module.infra.dal.dataobject.file.FileConfigDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 文件配置 Convert
 *
 * @author 书心软件
 */
@Mapper
public interface FileConfigConvert {

    FileConfigConvert INSTANCE = Mappers.getMapper(FileConfigConvert.class);

    @Mapping(target = "config", ignore = true)
    FileConfigDO convert(FileConfigSaveReqVO bean);

}
