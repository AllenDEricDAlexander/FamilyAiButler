package top.egon.familyaibutler.family.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import top.egon.familyaibutler.family.mapper.PasswordViewMapper;
import top.egon.familyaibutler.family.service.impl.PasswordViewServiceImpl;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family
 * @ClassName: TestPasswordViewService
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-03Day-10:48
 * @Description: TestPasswordViewService
 * @Version: 1.0
 */
@SpringBootTest
@ExtendWith(MockitoExtension.class)
class TestPasswordViewService {

    @InjectMocks
    private PasswordViewServiceImpl passwordViewService;

    @Mock
    private PasswordViewMapper passwordViewMapper;

    @Test
    void testPasswordStrength() {
        Assertions.assertFalse(PasswordViewServiceImpl.isValidPassword("12345678"));
        Assertions.assertFalse(PasswordViewServiceImpl.isValidPassword("q1w2e3r4"));
        Assertions.assertFalse(PasswordViewServiceImpl.isValidPassword("aaaa8888"));
        Assertions.assertFalse(PasswordViewServiceImpl.isValidPassword("Passw0rd!"));
        Assertions.assertTrue(PasswordViewServiceImpl.isValidPassword("xY7!pQ2@zR5#"));
    }

    @Test
    void testPasswordBase() {
        Mockito.when(passwordViewMapper.selectCount(ArgumentMatchers.any())).thenReturn(1L);
        Assertions.assertEquals(1L, passwordViewService.count());
    }
}