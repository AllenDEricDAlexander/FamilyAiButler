package top.egon.familyaibutler.common.mybatis.extention.function;

import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

import java.io.Serial;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.common.mybatis.extention.function
 * @ClassName: SelectByBusinessId
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-06Day-9:10
 * @Description: select by business id 扩展逻辑
 * @Version: 1.0
 */
public class SelectByBusinessId extends AbstractMethod {

    @Serial
    private static final long serialVersionUID = -9152505105219039615L;

    private static final String BUSINESS_ID = "business_id";
    private static final String BUSINESS_ID_PARAMETER = "businessId";

    /**
     * 创建根据业务主键查询方法。
     *
     * @param methodName 方法名称
     */
    public SelectByBusinessId(String methodName) {
        super(methodName);
    }

    /**
     * 注入根据业务主键查询的 MappedStatement。
     *
     * @param mapperClass Mapper 类型
     * @param modelClass  实体类型
     * @param tableInfo   表信息
     * @return MappedStatement 返回 MyBatis 映射语句
     */
    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        SqlMethod selectById = SqlMethod.SELECT_BY_ID;
        SqlSource sqlSource = super.createSqlSource(this.configuration,
                String.format(selectById.getSql(),
                        this.sqlSelectColumns(tableInfo, false),
                        tableInfo.getTableName(),
                        BUSINESS_ID,
                        BUSINESS_ID_PARAMETER,
                        tableInfo.getLogicDeleteSql(true, true)),
                Object.class);
        return this.addSelectMappedStatementForTable(mapperClass, this.methodName, sqlSource, tableInfo);
    }
}
