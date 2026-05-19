package top.egon.familyaibutler.family.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.hibernate.validator.constraints.Range;
import org.springframework.http.HttpHeaders;
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
import top.egon.familyaibutler.family.configuration.CacheService;
import top.egon.familyaibutler.family.domain.dto.PasswordViewDTO;
import top.egon.familyaibutler.family.domain.dto.StrengthDTO;
import top.egon.familyaibutler.family.po.CategoryDTO;
import top.egon.familyaibutler.family.po.CategoryPo;
import top.egon.familyaibutler.family.po.PasswordViewPO;
import top.egon.familyaibutler.family.po.convetor.CategoryConverter;
import top.egon.familyaibutler.family.service.PasswordViewService;
import top.egon.familyaibutler.family.service.impl.PasswordViewServiceImpl;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.domain.pojo
 * @ClassName: PasswordView
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-02Day-21:08
 * @Description: password view pojo
 * @Version: 1.0
 */
@RestController
@RequestMapping("/password")
@Validated
@Tag(name = "密码管理相关接口")
@Slf4j
@RequiredArgsConstructor
public class PasswordViewController {

    private static final String SPECIAL_CHARACTERS = "!@#$%^&*()-_=+<>?";

    private static final int PASSWORD_LENGTH = 12;

    private final PasswordViewService passwordViewService;
    private final CacheService cacheService;

    @GetMapping("/business/{businessId}")
    @Operation(summary = "通过业务主键查询单条数据", description = "通过业务主键查询单条数据")
    public Result<PasswordViewPO> selectOne(@PathVariable String businessId) {
        PasswordViewPO passwordView = cacheService.get(businessId, PasswordViewPO.class);
        if (ObjectUtils.isEmpty(passwordView)) {
            passwordView = this.passwordViewService.selectByBusinessId(businessId);
            cacheService.put(businessId, passwordView, 60L * 60 * 24);
        }
        return Result.success(passwordView);
    }

    @Operation(summary = "获取账号密码列表", description = "获取账号密码列表",
            parameters = {
                    @Parameter(name = "pageNum", description = "页码", in = ParameterIn.PATH, example = "1"),
                    @Parameter(name = "pageSize", description = "页大小", in = ParameterIn.PATH, example = "10"),
                    @Parameter(name = "passwordView", description = "查询条件", in = ParameterIn.PATH, example = "*")
            },
            responses = {
                    @ApiResponse(description = "返回一个字符串", responseCode = "10000", content = @Content(schema = @Schema(implementation = Result.class, description = "账号密码列表", name = "账号密码列表", title = "账号密码列表", example = "List<PasswordView>")))
            }
    )
    @GetMapping(value = {"/password/list/{pageNum}/{pageSize}", "/password/list"})
    public PageResult<List<PasswordViewPO>> selectAll(@PathVariable(value = "pageNum", required = false) @Range(min = 1) Integer pageNum,
                                                      @PathVariable(value = "pageSize", required = false) @Range(min = 1) Integer pageSize
            , @RequestBody(required = false) @Valid PasswordViewDTO passwordViewDTO) {
        int realPageNum = 1;
        int realPageSize = 10;
        if (ObjectUtils.isNotEmpty(pageNum)) {
            realPageNum = pageNum;
        }
        if (ObjectUtils.isNotEmpty(pageSize)) {
            realPageSize = pageSize;
        }
        PasswordViewPO.PasswordViewPOBuilder builder = PasswordViewPO.builder();
        if (ObjectUtils.isNotEmpty(passwordViewDTO)) {
            builder.name(passwordViewDTO.getName())
                    .password(passwordViewDTO.getPassword())
                    .description(passwordViewDTO.getDescription())
                    .accountNumber(passwordViewDTO.getAccountNumber())
                    .websit(passwordViewDTO.getWebsit())
                    .likeStatus(passwordViewDTO.isLikeStatus())
                    .category(passwordViewDTO.getCategory());
        }
        PasswordViewPO passwordView = builder.build();
        Page<PasswordViewPO> page = this.passwordViewService.page(new Page<>(realPageNum, realPageSize), new QueryWrapper<>(passwordView));
        return PageResult.success(Collections.singletonList(page.getRecords()), page.getTotal(), page.getCurrent(), page.getSize());
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("{id}")
    @Operation(summary = "通过主键查询单条数据", description = "通过主键查询单条数据")
    public Result<PasswordViewPO> selectOne(@PathVariable Long id) {
        PasswordViewPO passwordView = cacheService.get(String.valueOf(id), PasswordViewPO.class);
        if (ObjectUtils.isEmpty(passwordView)) {
            passwordView = this.passwordViewService.getById(id);
            cacheService.put(String.valueOf(id), passwordView, 60L * 60 * 24);
        }
        return Result.success(passwordView);
    }

    /**
     * 修改数据
     *
     * @param passwordViewDTO 实体对象
     * @return 修改结果
     */
    @PutMapping
    @Operation(summary = "修改数据", description = "修改数据", security = {@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)})
    public Result<Boolean> update(@RequestBody @Valid PasswordViewDTO passwordViewDTO) {
        PasswordViewPO byId = this.passwordViewService.getById(passwordViewDTO.getId());
        if (byId == null) {
            return Result.fail(10001, "未找到该数据", null);
        }
        cacheService.put(byId.getBusinessId(), byId, 60L * 60 * 24);
        byId.setName(passwordViewDTO.getName())
                .setPassword(passwordViewDTO.getPassword())
                .setDescription(passwordViewDTO.getDescription())
                .setAccountNumber(passwordViewDTO.getAccountNumber())
                .setWebsit(passwordViewDTO.getWebsit())
                .setLikeStatus(passwordViewDTO.isLikeStatus())
                .setCategory(passwordViewDTO.getCategory());
        return Result.success(this.passwordViewService.updateById(byId));
    }

    /**
     * 删除数据
     *
     * @param idList 主键结合
     * @return 删除结果
     */
    @DeleteMapping
    @Operation(summary = "删除数据", description = "删除数据")
    public Result<Boolean> delete(@RequestBody List<Long> idList) {
        idList.forEach(id -> cacheService.evict(String.valueOf(id)));
        return Result.success(this.passwordViewService.removeByIds(idList));
    }

    @Operation(summary = "添加一个账号密码", description = "添加一个账号密码",
            parameters = {
                    @Parameter(name = "passwordView", description = "passwordView", in = ParameterIn.DEFAULT, required = true, example = "PasswordView")
            },
            responses = {
                    @ApiResponse(description = "返回是否添加成功", responseCode = "10000", content = @Content(schema = @Schema(implementation = Result.class, description = "添加结果", name = "添加结果", title = "添加结果", example = "添加成功")))
            }
    )
    @PostMapping("/password/add")
    public Result<String> add(@RequestBody @Valid PasswordViewDTO passwordViewDTO) {
        PasswordViewPO passwordView = PasswordViewPO.builder()
                .name(passwordViewDTO.getName())
                .password(passwordViewDTO.getPassword())
                .description(passwordViewDTO.getDescription())
                .accountNumber(passwordViewDTO.getAccountNumber())
                .websit(passwordViewDTO.getWebsit())
                .likeStatus(passwordViewDTO.isLikeStatus())
                .category(passwordViewDTO.getCategory())
                .build();
        boolean save = passwordViewService.save(passwordView);
        return save ? Result.success("添加成功") : Result.fail(10001, "添加失败", null);
    }


    @Operation(summary = "生成一个随机密码", description = "生成一个随机密码",
            parameters = {
                    @Parameter(name = "passwordLength", description = "passwordLength", in = ParameterIn.PATH, required = true, example = "16"),
                    @Parameter(name = "needSpecialCharacters", description = "needSpecialCharacters", in = ParameterIn.PATH, example = "true"),
                    @Parameter(name = "specialCharacters", description = "specialCharacters", in = ParameterIn.PATH, example = "!@#$%^&*()-_=+<>?")
            },
            responses = {
                    @ApiResponse(description = "返回一个字符串", responseCode = "10000", content = @Content(schema = @Schema(implementation = Result.class, description = "随机密码", name = "随机密码", title = "随机密码", example = "W7%@pQJt16ZeN&2u")))
            }
    )
    @GetMapping(value = {"/generate/{passwordLength}", "/generate/{passwordLength}/{needSpecialCharacters}", "/generate/{passwordLength}/{needSpecialCharacters}/{specialCharacters}"})
    public Result<String> generatePassword(@PathVariable(value = "passwordLength") @Range(min = 12, max = 24, message = "密码生成长度在12-24之间") Integer passwordLength,
                                           @PathVariable(value = "needSpecialCharacters", required = false) Boolean needSpecialCharacters,
                                           @PathVariable(value = "specialCharacters", required = false) String specialCharacters) {
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
        return Result.success(PasswordViewServiceImpl.generatePassword(realLength, isRealNeed, realSpecialCharacters));
    }

    @Operation(summary = "检查密码强度", description = "检查密码强度",
            parameters = {
                    @Parameter(name = "password", description = "password", in = ParameterIn.PATH, required = true, example = "xY7!pQ2@zR5#")
            }
//            ,
//            responses = {
//                    @ApiResponse(description = "返回一个密码强度对象", responseCode = "10000", content = @Content(schema = @Schema(implementation = Result.class, description = "密码强度", name = "密码强度", title = "密码强度", example = "*")))
//            }
    )
    @GetMapping("/checkStrength/{password}")
    public Result<StrengthDTO> checkPassword(@PathVariable String password) {
        return Result.success(PasswordViewServiceImpl.checkStrength(password));
    }

    @Operation(summary = "检查密码强度", description = "检查密码强度",
            parameters = {
                    @Parameter(name = "password", description = "password", in = ParameterIn.PATH, required = true, example = "xY7!pQ2@zR5#")
            },
            responses = {
                    @ApiResponse(description = "返回一个密码强度对象", responseCode = "10000", content = @Content(schema = @Schema(implementation = Boolean.class, description = "密码强度", name = "密码强度", title = "密码强度", example = "true")))
            }
    )
    @GetMapping("/checkValid/{password}")
    public Result<Boolean> checkValid(@PathVariable String password) {
        CategoryPo source = CategoryPo.builder().id(1L).name("abc").description("abc").parentId(0L).categoryType(null).createTime(new Date()).updateTime(new Date()).build();

        // 单个对象转换
        CategoryDTO target = CategoryConverter.INSTANCE.toTarget(source);
        System.out.println(target);

        // 列表转换
        List<CategoryDTO> targets = CategoryConverter.INSTANCE.toTargetList(List.of(source));
        System.out.println(targets);
        return Result.success(PasswordViewServiceImpl.isValidPassword(password));
    }

}

