package top.egon.familyaibutler.family.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import top.egon.familyaibutler.common.pojo.Result;
import top.egon.familyaibutler.family.FamilyApplication;
import top.egon.familyaibutler.family.infrastructure.configuration.CacheService;
import top.egon.familyaibutler.family.infrastructure.persistence.mp.dataobject.PasswordViewPO;
import top.egon.familyaibutler.family.infrastructure.persistence.mp.service.PasswordViewService;

import java.util.Random;
import java.util.regex.Pattern;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.adapter
 * @ClassName: TestPassword
 * @Author: atluofu
 * @CreateTime: 2025Year-07Month-31Day-22:20
 * @Description: 密码管理器相关单元测试
 * @Version: 1.0
 */
@SpringBootTest(classes = FamilyApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
class TestPassword {

    private static final String PASSWORD_TRUE_REGEX = "^[a-zA-Z0-9!@#$%^&*()-_=+<>?]+$";
    private static final String PASSWORD_FALSE_REGEX = "^[a-zA-Z0-9]+$";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PasswordViewService passwordViewService;

    @MockitoBean
    private CacheService cacheService;

    @Test
    void testSelectOne() throws Exception {
        PasswordViewPO passwordViewPO1 = PasswordViewPO.builder()
                .id(1L)
                .accountNumber("001")
                .name("name001").build();
        Mockito.when(passwordViewService.getById(1L)).thenReturn(passwordViewPO1);
        mockMvc.perform(MockMvcRequestBuilders.get("/password/1")
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(10000))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("success"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.accountNumber").value("001"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.name").value("name001"))
                .andExpect(result -> {
                    ObjectMapper objectMapper = new ObjectMapper();
                    Result data = objectMapper.readValue(result.getResponse().getContentAsString(), Result.class);
                    Assertions.assertNotNull(data);
                })
                .andReturn();
        Mockito.verify(cacheService).put(Mockito.eq("1"), Mockito.same(passwordViewPO1), Mockito.anyLong());
    }

    @Test
    void testSelectOneReturnsSuccessWhenPasswordDoesNotExist() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/password/404")
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(10000))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("success"))
                .andExpect(result -> {
                    ObjectMapper objectMapper = new ObjectMapper();
                    Result data = objectMapper.readValue(result.getResponse().getContentAsString(), Result.class);
                    Assertions.assertNull(data.getData());
                })
                .andReturn();
        Mockito.verify(cacheService, Mockito.never()).put(Mockito.eq("404"), Mockito.isNull(), Mockito.anyLong());
    }

    @Test
    @DisplayName("单个密码随机生成单元测试")
    void testPassword() throws Exception {
        int length = new Random().nextInt(16, 24);
        ObjectMapper objectMapper = new ObjectMapper();
        mockMvc.perform(MockMvcRequestBuilders.get("/password/generate/" + length)
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(10000))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("success"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                .andExpect(result -> {
                    String data = objectMapper.readValue(result.getResponse().getContentAsString(), Result.class).getData().toString();
                    Assertions.assertEquals(length, data.length());
                    Assertions.assertTrue(Pattern.matches(PASSWORD_TRUE_REGEX, data));
                })
                .andReturn();
        mockMvc.perform(MockMvcRequestBuilders.get("/password/generate/" + length + "/false")
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(10000))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("success"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                .andExpect(result -> {
                    String data = objectMapper.readValue(result.getResponse().getContentAsString(), Result.class).getData().toString();
                    Assertions.assertEquals(length, data.length());
                    Assertions.assertTrue(Pattern.matches(PASSWORD_FALSE_REGEX, data));
                })
                .andReturn();

    }


}
