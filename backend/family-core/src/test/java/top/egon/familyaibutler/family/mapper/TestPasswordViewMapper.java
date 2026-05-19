package top.egon.familyaibutler.family.mapper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.repository.mapper
 * @ClassName: TestPasswordViewMapper
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-02Day-21:24
 * @Description: TestPasswordViewMapper
 * @Version: 1.0
 */
@SpringBootTest
class TestPasswordViewMapper {
    @Mock
    private PasswordViewMapper passwordViewMapper;

    @Test
    void testSelect() {
        Long count = 1L;
        Mockito.when(passwordViewMapper.selectCount(null)).thenReturn(count);
        Long l = passwordViewMapper.selectCount(null);
        Assertions.assertEquals(count,l);
    }

}