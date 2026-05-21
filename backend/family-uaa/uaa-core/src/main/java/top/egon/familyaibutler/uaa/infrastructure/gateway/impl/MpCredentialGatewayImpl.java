/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.infrastructure.gateway.impl
 * @FileName: MpCredentialGatewayImpl.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-14:35
 * @Description: MyBatis Plus 凭证网关实现文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.infrastructure.gateway.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Repository;
import top.egon.familyaibutler.uaa.domain.account.gateway.CredentialGateway;
import top.egon.familyaibutler.uaa.domain.account.model.entity.Credential;
import top.egon.familyaibutler.uaa.domain.account.model.enums.CredentialType;
import top.egon.familyaibutler.uaa.infrastructure.persistence.mp.converter.UaaMpConverter;
import top.egon.familyaibutler.uaa.infrastructure.persistence.mp.dataobject.CredentialPO;
import top.egon.familyaibutler.uaa.infrastructure.persistence.mp.mapper.CredentialMapper;

import java.util.Optional;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.infrastructure.gateway.impl
 * @ClassName: MpCredentialGatewayImpl
 * @Author: atluofu
 * @CreateTime: 2026-05-20 14:35
 * @Description: MyBatis Plus 凭证网关实现
 * @Version: 1.0
 */
@Repository
public class MpCredentialGatewayImpl implements CredentialGateway {
    private final CredentialMapper credentialMapper;
    private final UaaMpConverter uaaMpConverter;

    /**
     * 创建 MyBatis Plus 凭证网关实现。
     *
     * @param credentialMapper 凭证 Mapper
     * @param uaaMpConverter   UAA 转换器
     */
    public MpCredentialGatewayImpl(CredentialMapper credentialMapper, UaaMpConverter uaaMpConverter) {
        this.credentialMapper = credentialMapper;
        this.uaaMpConverter = uaaMpConverter;
    }

    /**
     * 保存凭证。
     *
     * @param credential 凭证实体
     * @return 保存后的凭证实体
     */
    @Override
    public Credential save(Credential credential) {
        CredentialPO credentialPO = uaaMpConverter.toCredentialPO(credential);
        if (credentialMapper.selectById(credentialPO.getCredentialId()) == null) {
            credentialMapper.insert(credentialPO);
        } else {
            credentialMapper.updateById(credentialPO);
        }
        return credential;
    }

    /**
     * 查询账号的密码凭证。
     *
     * @param accountId 账号 ID
     * @return 密码凭证
     */
    @Override
    public Optional<Credential> findPasswordCredential(String accountId) {
        LambdaQueryWrapper<CredentialPO> wrapper = new LambdaQueryWrapper<CredentialPO>()
                .eq(CredentialPO::getAccountId, accountId)
                .eq(CredentialPO::getCredentialType, CredentialType.PASSWORD.name())
                .last("limit 1");
        return Optional.ofNullable(credentialMapper.selectOne(wrapper)).map(uaaMpConverter::toCredential);
    }
}
