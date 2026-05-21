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
private final ${serviceInterfaceName} ${serviceFieldName};
${methods}
}
