package com.ruipeng.planner.repository;

import com.ruipeng.planner.entity.AccountStatus;
import com.ruipeng.planner.entity.Advisor;
import com.ruipeng.planner.entity.User;
import com.ruipeng.planner.entity.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Optional;

@DataJpaTest
public class AdvisorRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AdvisorRepository advisorRepository;

    User prepare_new_user(){
        User user = new User();
        user.setEmail("prepare_new_user@gmail.com");
        user.setPasswordHash("123456");
        user.setRole(UserRole.USER);
        user.setStatus(AccountStatus.ACTIVE);
        user.setFirstName("TestFirstName");
        user.setLastName("TestLastName");
        user.setRegistrationDate(LocalDateTime.now());
        entityManager.persist(user);
        return user;
    }

    Advisor prepare_new_advisor(){
        Advisor advisor = new Advisor();
        advisor.setUser(prepare_new_user());
        return  advisor;
    }

    @Test
    void should_find_advisor_by_userId() {
        //arrange
        Advisor advisor = prepare_new_advisor();
        entityManager.persistAndFlush(advisor);

        //act
        Optional<Advisor> advisor_find_byUserId = advisorRepository.findByUserId(advisor.getUser().getId());

        //assert
        assertThat(advisor_find_byUserId.isPresent()).isTrue();
        assertThat(advisor_find_byUserId.get().getUser().getFirstName().equals("TestFirstName")).isTrue();

    }
}
