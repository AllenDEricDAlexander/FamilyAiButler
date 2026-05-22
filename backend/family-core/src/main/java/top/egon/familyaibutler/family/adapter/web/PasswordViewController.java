package top.egon.familyaibutler.family.adapter.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.hibernate.validator.constraints.Range;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.egon.familyaibutler.common.pojo.PageResult;
import top.egon.familyaibutler.common.pojo.Result;
import top.egon.familyaibutler.family.adapter.web.assembler.PasswordViewWebAssembler;
import top.egon.familyaibutler.family.application.manage.PasswordViewManage;
import top.egon.familyaibutler.family.application.query.PasswordViewPageQuery;
import top.egon.familyaibutler.family.application.result.PasswordViewDTO;
import top.egon.familyaibutler.family.domain.passwordview.model.valueobject.StrengthDTO;
import top.egon.openapi.console.annotation.DocBody;
import top.egon.openapi.console.annotation.DocDataKind;
import top.egon.openapi.console.annotation.DocDataType;
import top.egon.openapi.console.annotation.DocOperation;
import top.egon.openapi.console.annotation.DocParam;
import top.egon.openapi.console.annotation.DocParamIn;
import top.egon.openapi.console.annotation.DocParameter;
import top.egon.openapi.console.annotation.DocProtocol;
import top.egon.openapi.console.annotation.DocRequest;
import top.egon.openapi.console.annotation.DocResponse;
import top.egon.openapi.console.annotation.DocService;
import top.egon.openapi.console.annotation.DocTypeReference;
import top.egon.openapi.console.annotation.DocWrapper;

import java.util.List;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.adapter.web
 * @ClassName: PasswordViewController
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-02Day-21:08
 * @Description: 账号密码 Web 适配器
 * @Version: 1.0
 */
@RestController
@RequestMapping("/password")
@Validated
@DocService(groupId = "core", groupName = "家庭核心服务", serviceId = "family-core-password",
        serviceName = "密码管理相关接口", serviceDescription = "账号密码管理接口", protocol = DocProtocol.HTTP)
@Slf4j
@RequiredArgsConstructor
public class PasswordViewController {

    private static final String SPECIAL_CHARACTERS = "!@#$%^&*()-_=+<>?";

    private static final int PASSWORD_LENGTH = 12;

    private final PasswordViewManage passwordViewService;
    private final PasswordViewWebAssembler passwordViewWebAssembler;

    @GetMapping("/business/{businessId}")
    @DocOperation(summary = "通过业务主键查询单条数据", description = "通过业务主键查询单条数据",
            response = @DocResponse(description = "查询成功",
                    dataType = @DocDataType(kind = DocDataKind.OBJECT, type = PasswordViewDTO.class),
                    wrapper = @DocWrapper(type = Result.class, dataPath = "data")))
    public Result<PasswordViewDTO> selectOne(@PathVariable @DocParam(description = "业务主键", required = true) String businessId) {
        return Result.success(passwordViewService.selectByBusinessId(businessId));
    }

    @DocOperation(summary = "获取账号密码列表", description = "获取账号密码列表",
            request = @DocRequest(
                    params = {
                            @DocParameter(name = "pageNum", in = DocParamIn.PATH, description = "页码",
                                    dataType = @DocDataType(kind = DocDataKind.INTEGER), example = "1"),
                            @DocParameter(name = "pageSize", in = DocParamIn.PATH, description = "页大小",
                                    dataType = @DocDataType(kind = DocDataKind.INTEGER), example = "10")
                    },
                    body = @DocBody(enabled = true, description = "查询条件", required = false,
                            dataType = @DocDataType(kind = DocDataKind.OBJECT, type = PasswordViewDTO.class))),
            response = @DocResponse(description = "查询成功",
                    dataType = @DocDataType(kind = DocDataKind.GENERIC, ref = PasswordViewPageDataType.class)))
    @GetMapping(value = {"/password/list/{pageNum}/{pageSize}", "/password/list"})
    public PageResult<PasswordViewDTO> selectAll(@PathVariable(value = "pageNum", required = false) @DocParam(description = "页码") @Range(min = 1) Integer pageNum,
                                                 @PathVariable(value = "pageSize", required = false) @DocParam(description = "页大小") @Range(min = 1) Integer pageSize
            , @RequestBody(required = false) @Valid PasswordViewDTO passwordViewDTO) {
        PasswordViewPageQuery query = passwordViewWebAssembler.toPageQuery(pageNum, pageSize, passwordViewDTO);
        return passwordViewService.page(query);
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("{id}")
    @DocOperation(summary = "通过主键查询单条数据", description = "通过主键查询单条数据",
            response = @DocResponse(description = "查询成功",
                    dataType = @DocDataType(kind = DocDataKind.OBJECT, type = PasswordViewDTO.class),
                    wrapper = @DocWrapper(type = Result.class, dataPath = "data")))
    public Result<PasswordViewDTO> selectOne(@PathVariable @DocParam(description = "主键", required = true) Long id) {
        return Result.success(passwordViewService.selectById(id));
    }

    /**
     * 修改数据
     *
     * @param passwordViewDTO 实体对象
     * @return 修改结果
     */
    @PutMapping
    @DocOperation(summary = "修改数据", description = "修改数据",
            request = @DocRequest(body = @DocBody(dataType = @DocDataType(kind = DocDataKind.OBJECT, type = PasswordViewDTO.class))),
            response = @DocResponse(description = "修改成功",
                    dataType = @DocDataType(kind = DocDataKind.BOOLEAN),
                    wrapper = @DocWrapper(type = Result.class, dataPath = "data")))
    public Result<Boolean> update(@RequestBody @Valid PasswordViewDTO passwordViewDTO) {
        boolean updated = passwordViewService.update(passwordViewWebAssembler.toUpdateCommand(passwordViewDTO));
        if (!updated) {
            return Result.fail(10001, "未找到该数据", null);
        }
        return Result.success(true);
    }

    /**
     * 删除数据
     *
     * @param idList 主键结合
     * @return 删除结果
     */
    @DeleteMapping
    @DocOperation(summary = "删除数据", description = "删除数据",
            request = @DocRequest(body = @DocBody(dataType = @DocDataType(kind = DocDataKind.GENERIC, ref = LongListDataType.class))),
            response = @DocResponse(description = "删除成功",
                    dataType = @DocDataType(kind = DocDataKind.BOOLEAN),
                    wrapper = @DocWrapper(type = Result.class, dataPath = "data")))
    public Result<Boolean> delete(@RequestBody List<Long> idList) {
        return Result.success(passwordViewService.delete(idList));
    }

    @DocOperation(summary = "添加一个账号密码", description = "添加一个账号密码",
            request = @DocRequest(body = @DocBody(dataType = @DocDataType(kind = DocDataKind.OBJECT, type = PasswordViewDTO.class))),
            response = @DocResponse(description = "添加成功",
                    dataType = @DocDataType(kind = DocDataKind.STRING),
                    wrapper = @DocWrapper(type = Result.class, dataPath = "data")))
    @PostMapping("/password/add")
    public Result<String> add(@RequestBody @Valid PasswordViewDTO passwordViewDTO) {
        boolean save = passwordViewService.create(passwordViewWebAssembler.toCreateCommand(passwordViewDTO));
        return save ? Result.success("添加成功") : Result.fail(10001, "添加失败", null);
    }


    @DocOperation(summary = "生成一个随机密码", description = "生成一个随机密码",
            response = @DocResponse(description = "生成成功",
                    dataType = @DocDataType(kind = DocDataKind.STRING),
                    wrapper = @DocWrapper(type = Result.class, dataPath = "data")))
    @GetMapping(value = {"/generate/{passwordLength}", "/generate/{passwordLength}/{needSpecialCharacters}", "/generate/{passwordLength}/{needSpecialCharacters}/{specialCharacters}"})
    public Result<String> generatePassword(@PathVariable(value = "passwordLength") @DocParam(description = "passwordLength", required = true) @Range(min = 12, max = 24, message = "密码生成长度在12-24之间") Integer passwordLength,
                                           @PathVariable(value = "needSpecialCharacters", required = false) @DocParam(description = "needSpecialCharacters") Boolean needSpecialCharacters,
                                           @PathVariable(value = "specialCharacters", required = false) @DocParam(description = "specialCharacters") String specialCharacters) {
        int realLength = PASSWORD_LENGTH;
        if (ObjectUtils.isNotEmpty(passwordLength)) {
            realLength = passwordLength;
        }
        boolean isRealNeed = true;
        if (ObjectUtils.isNotEmpty(needSpecialCharacters)) {
            isRealNeed = needSpecialCharacters;
        }
        String realSpecialCharacters = SPECIAL_CHARACTERS;
        if (ObjectUtils.isNotEmpty(specialCharacters)) {
            realSpecialCharacters = specialCharacters;
        }
        return Result.success(passwordViewService.generatePassword(realLength, isRealNeed, realSpecialCharacters));
    }

    @DocOperation(summary = "检查密码强度", description = "检查密码强度",
            response = @DocResponse(description = "检查成功",
                    dataType = @DocDataType(kind = DocDataKind.OBJECT, type = StrengthDTO.class),
                    wrapper = @DocWrapper(type = Result.class, dataPath = "data")))
    @GetMapping("/checkStrength/{password}")
    public Result<StrengthDTO> checkPassword(@PathVariable @DocParam(description = "password", required = true) String password) {
        return Result.success(passwordViewService.checkStrength(password));
    }

    @DocOperation(summary = "检查密码强度", description = "检查密码强度",
            response = @DocResponse(description = "检查成功",
                    dataType = @DocDataType(kind = DocDataKind.BOOLEAN),
                    wrapper = @DocWrapper(type = Result.class, dataPath = "data")))
    @GetMapping("/checkValid/{password}")
    public Result<Boolean> checkValid(@PathVariable @DocParam(description = "password", required = true) String password) {
        return Result.success(passwordViewService.checkValid(password));
    }

    public static final class PasswordViewPageDataType extends DocTypeReference<PageResult<PasswordViewDTO>> {
    }

    public static final class LongListDataType extends DocTypeReference<List<Long>> {
    }

}
