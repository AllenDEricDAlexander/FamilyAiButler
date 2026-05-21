/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.infrastructure.gateway.impl
 * @FileName: MpAccountGatewayImpl.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-14:35
 * @Description: MyBatis Plus 账号网关实现文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.infrastructure.gateway.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Repository;
import top.egon.familyaibutler.uaa.domain.account.gateway.AccountGateway;
import top.egon.familyaibutler.uaa.domain.account.model.aggregate.Account;
import top.egon.familyaibutler.uaa.infrastructure.persistence.mp.converter.UaaMpConverter;
import top.egon.familyaibutler.uaa.infrastructure.persistence.mp.dataobject.AccountPO;
import top.egon.familyaibutler.uaa.infrastructure.persistence.mp.mapper.AccountMapper;

import java.util.Optional;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.infrastructure.gateway.impl
 * @ClassName: MpAccountGatewayImpl
 * @Author: atluofu
 * @CreateTime: 2026-05-20 14:35
 * @Description: MyBatis Plus 账号网关实现
 * @Version: 1.0
 */
@Repository
public class MpAccountGatewayImpl implements AccountGateway {
    private final AccountMapper accountMapper;
    private final UaaMpConverter uaaMpConverter;

    /**
     * 创建 MyBatis Plus 账号网关实现。
     *
     * @param accountMapper  账号 Mapper
     * @param uaaMpConverter UAA 转换器
     */
    public MpAccountGatewayImpl(AccountMapper accountMapper, UaaMpConverter uaaMpConverter) {
        this.accountMapper = accountMapper;
        this.uaaMpConverter = uaaMpConverter;
    }

    /**
     * 保存账号聚合。
     *
     * @param account 账号聚合
     * @return 保存后的账号聚合
     */
    @Override
    public Account save(Account account) {
        AccountPO accountPO = uaaMpConverter.toAccountPO(account);
        if (accountMapper.selectById(account.getAccountId()) == null) {
            accountMapper.insert(accountPO);
        } else {
            accountMapper.updateById(accountPO);
        }
        return account;
    }

    /**
     * 按账号 ID 查询账号。
     *
     * @param accountId 账号 ID
     * @return 账号聚合
     */
    @Override
    public Optional<Account> findByAccountId(String accountId) {
        return Optional.ofNullable(accountMapper.selectById(accountId)).map(uaaMpConverter::toAccount);
    }

    /**
     * 按用户名查询账号。
     *
     * @param username 用户名
     * @return 账号聚合
     */
    @Override
    public Optional<Account> findByUsername(String username) {
        return findOne(AccountPO::getUsername, username);
    }

    /**
     * 按邮箱查询账号。
     *
     * @param email 邮箱
     * @return 账号聚合
     */
    @Override
    public Optional<Account> findByEmail(String email) {
        return findOne(AccountPO::getEmail, email);
    }

    /**
     * 按手机号查询账号。
     *
     * @param phone 手机号
     * @return 账号聚合
     */
    @Override
    public Optional<Account> findByPhone(String phone) {
        return findOne(AccountPO::getPhone, phone);
    }

    /**
     * 按字段查询单个账号。
     *
     * @param column 查询字段
     * @param value  查询值
     * @return 账号聚合
     */
    private Optional<Account> findOne(com.baomidou.mybatisplus.core.toolkit.support.SFunction<AccountPO, ?> column, String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        LambdaQueryWrapper<AccountPO> wrapper = new LambdaQueryWrapper<AccountPO>().eq(column, value).last("limit 1");
        return Optional.ofNullable(accountMapper.selectOne(wrapper)).map(uaaMpConverter::toAccount);
    }
}
