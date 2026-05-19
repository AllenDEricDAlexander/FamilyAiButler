package top.egon.familyaibutler.common.extention.function;

import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

import java.io.Serial;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.common.extention.function
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

    public SelectByBusinessId(String methodName) {
        super(methodName);
    }

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