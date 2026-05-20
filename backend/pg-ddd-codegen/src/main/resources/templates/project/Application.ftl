package ${packageName};

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

${classComment}@SpringBootApplication
public class ${className} {

/**
* 应用启动入口。
*
* @param args 启动参数
*/
public static void main(String[] args) {
SpringApplication.run(${className}.class, args);
}
}
