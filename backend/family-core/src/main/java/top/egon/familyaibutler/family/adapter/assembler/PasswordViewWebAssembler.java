package top.egon.familyaibutler.family.adapter.assembler;

import org.springframework.stereotype.Component;
import top.egon.familyaibutler.family.application.dto.CreatePasswordViewCommand;
import top.egon.familyaibutler.family.application.dto.UpdatePasswordViewCommand;
import top.egon.familyaibutler.family.application.dto.PasswordViewPageQuery;
import top.egon.familyaibutler.family.application.dto.PasswordViewDTO;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.adapter.assembler
 * @ClassName: PasswordViewWebAssembler
 * @Author: atluofu
 * @CreateTime: 2026-05-20 10:30
 * @Description: 账号密码 Web 入参装配器
 * @Version: 1.0
 */
@Component
public class PasswordViewWebAssembler {

    /**
     * DTO 转创建命令。
     *
     * @param dto Web 入参
     * @return 创建命令
     */
    public CreatePasswordViewCommand toCreateCommand(PasswordViewDTO dto) {
        return new CreatePasswordViewCommand(dto.getName(), dto.getPassword(), dto.getDescription(),
                dto.getAccountNumber(), dto.getWebsit(), dto.isLikeStatus(), dto.getCategory());
    }

    /**
     * DTO 转修改命令。
     *
     * @param dto Web 入参
     * @return 修改命令
     */
    public UpdatePasswordViewCommand toUpdateCommand(PasswordViewDTO dto) {
        return new UpdatePasswordViewCommand(dto.getId(), dto.getName(), dto.getPassword(), dto.getDescription(),
                dto.getAccountNumber(), dto.getWebsit(), dto.isLikeStatus(), dto.getCategory());
    }

    /**
     * DTO 转分页查询对象。
     *
     * @param pageNum  页码
     * @param pageSize 页大小
     * @param dto      查询入参
     * @return 分页查询对象
     */
    public PasswordViewPageQuery toPageQuery(Integer pageNum, Integer pageSize, PasswordViewDTO dto) {
        if (dto == null) {
            return new PasswordViewPageQuery(pageNum, pageSize, null, null, null, null, null, null, null);
        }
        return new PasswordViewPageQuery(pageNum, pageSize, dto.getName(), dto.getPassword(), dto.getDescription(),
                dto.getAccountNumber(), dto.getWebsit(), dto.isLikeStatus(), dto.getCategory());
    }
}
