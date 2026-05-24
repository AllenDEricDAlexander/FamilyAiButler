package ${packageName};

${imports!""}import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

${classComment}@RestController
@RequestMapping("${requestMapping}")
@RequiredArgsConstructor
public class ${className} {

/**
* ${aggregateName} 应用服务接口。
*/
private final ${manageInterfaceName} ${manageFieldName};
/**
* ${aggregateName} Web 对象转换器。
*/
private final ${webAssemblerName} ${webAssemblerFieldName};

${methods}
}
