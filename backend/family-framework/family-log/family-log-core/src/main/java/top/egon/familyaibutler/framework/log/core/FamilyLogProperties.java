/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.core
 * @FileName: FamilyLogProperties.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-20:40
 * @Description: 家庭日志组件配置属性文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.framework.log.core;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.core
 * @ClassName: FamilyLogProperties
 * @Author: atluofu
 * @CreateTime: 2026-05-21 20:40
 * @Description: 家庭日志组件配置属性
 * @Version: 1.0
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "family.log")
public class FamilyLogProperties {

    private boolean enabled = true;

    private boolean mdcEnabled = true;

    private boolean requestLogEnabled = true;

    private boolean servletEnabled = true;

    private boolean webfluxEnabled = true;

    private boolean httpEnabled = true;

    private boolean dubboEnabled = true;

    private boolean grpcEnabled = true;

    private boolean asyncEnabled = true;

    private boolean responseTraceHeaderEnabled = true;

    private int maxPayloadLength = 2000;

    private List<String> traceHeaderNames = new ArrayList<>(FamilyLogMdcKeys.defaultTraceHeaders());

    private List<String> requestIdHeaderNames = new ArrayList<>(FamilyLogMdcKeys.defaultRequestIdHeaders());

    private List<String> propagationHeaderNames = new ArrayList<>(FamilyLogMdcKeys.propagationHeaders());
}
