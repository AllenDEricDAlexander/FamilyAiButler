package top.egon.familyaibutler.uaa.vo;

import lombok.Data;

import java.util.List;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.vo
 * @ClassName: UserPermissionsVO
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-13Day-10:29
 * @Description: UserPermissionsVO
 * @Version: 1.0
 */
@Data
public class UserPermissionsVO {
    private Long id;
    private String username;
    private Boolean enabled;
    private List<String> permissions;
}