/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.infrastructure.gatewayimpl
 * @FileName: MpOAuthClientGatewayImpl.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-15:00
 * @Description: MyBatis Plus OAuth Client 网关实现文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.infrastructure.gatewayimpl;

import org.springframework.stereotype.Repository;
import top.egon.familyaibutler.uaa.domain.gateway.OAuthClientGateway;
import top.egon.familyaibutler.uaa.domain.model.aggregate.OAuthClient;
import top.egon.familyaibutler.uaa.infrastructure.persistence.mp.converter.UaaMpConverter;
import top.egon.familyaibutler.uaa.infrastructure.persistence.mp.dataobject.OAuthClientPO;
import top.egon.familyaibutler.uaa.infrastructure.persistence.mp.mapper.OAuthClientMapper;

import java.util.List;
import java.util.Optional;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.infrastructure.gatewayimpl
 * @ClassName: MpOAuthClientGatewayImpl
 * @Author: atluofu
 * @CreateTime: 2026-05-20 15:00
 * @Description: MyBatis Plus OAuth Client 网关实现
 * @Version: 1.0
 */
@Repository
public class MpOAuthClientGatewayImpl implements OAuthClientGateway {
    private final OAuthClientMapper oAuthClientMapper;
    private final UaaMpConverter uaaMpConverter;

    /**
     * 创建 MyBatis Plus OAuth Client 网关实现。
     *
     * @param oAuthClientMapper OAuth Client Mapper
     * @param uaaMpConverter    UAA 转换器
     */
    public MpOAuthClientGatewayImpl(OAuthClientMapper oAuthClientMapper, UaaMpConverter uaaMpConverter) {
        this.oAuthClientMapper = oAuthClientMapper;
        this.uaaMpConverter = uaaMpConverter;
    }

    /**
     * 保存 OAuth Client。
     *
     * @param client OAuth Client
     * @return OAuth Client
     */
    @Override
    public OAuthClient save(OAuthClient client) {
        OAuthClientPO clientPO = uaaMpConverter.toOAuthClientPO(client);
        if (oAuthClientMapper.selectById(client.getClientId()) == null) {
            oAuthClientMapper.insert(clientPO);
        } else {
            oAuthClientMapper.updateById(clientPO);
        }
        return client;
    }

    /**
     * 按客户端 ID 查询。
     *
     * @param clientId 客户端 ID
     * @return OAuth Client
     */
    @Override
    public Optional<OAuthClient> findByClientId(String clientId) {
        return Optional.ofNullable(oAuthClientMapper.selectById(clientId)).map(uaaMpConverter::toOAuthClient);
    }

    /**
     * 查询全部 OAuth Client。
     *
     * @return OAuth Client 列表
     */
    @Override
    public List<OAuthClient> findAll() {
        return oAuthClientMapper.selectList(null).stream().map(uaaMpConverter::toOAuthClient).toList();
    }
}
