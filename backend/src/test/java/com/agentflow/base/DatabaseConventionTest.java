package com.agentflow.base;

import static org.assertj.core.api.Assertions.assertThat;

import com.agentflow.base.model.bo.BaseEntity;
import com.agentflow.base.model.bo.Role;
import com.agentflow.base.model.bo.TestRecord;
import com.agentflow.base.model.bo.UserAccount;
import com.agentflow.base.model.bo.UserRole;
import jakarta.persistence.Table;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DatabaseConventionTest {
    @Test
    void everyEntityUsesSingularSnakeCaseAndUuidBaseId() throws Exception {
        for (Class<?> entity : List.of(Role.class, TestRecord.class, UserAccount.class, UserRole.class)) {
            String table = entity.getAnnotation(Table.class).name();
            assertThat(table).matches("[a-z][a-z0-9_]*").doesNotEndWith("s");
            assertThat(entity.getSuperclass()).isEqualTo(BaseEntity.class);
        }
        assertThat(BaseEntity.class.getDeclaredField("id").getType()).isEqualTo(UUID.class);
        assertThat(BaseEntity.class.getDeclaredField("createdAt")).isNotNull();
        assertThat(BaseEntity.class.getDeclaredField("updatedAt")).isNotNull();
    }
}

