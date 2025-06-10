package com.ruipeng.planner.service;

import com.ruipeng.planner.dto.AdvisorProfileDto;
import com.ruipeng.planner.entity.AccountStatus;
import com.ruipeng.planner.entity.Advisor;
import com.ruipeng.planner.entity.User;
import com.ruipeng.planner.entity.UserRole;
import com.ruipeng.planner.repository.AdvisorRepository;
import com.ruipeng.planner.repository.AppointmentRepository;
import com.ruipeng.planner.repository.AvailabilitySlotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AdvisorServiceTest {

    @Mock
    private AdvisorRepository advisorRepository;

    @Mock
    private AvailabilitySlotRepository availabilitySlotRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private AdvisorService advisorService;

    private Advisor createMockAdvisor(Long id, String firstName, String lastName,
                                      String bio, Integer experienceYears, Double rating,
                                      String profileImageUrl, Set<String> specialties) {

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(firstName.toLowerCase() + "." + lastName.toLowerCase() + "@example.com");
        user.setRole(UserRole.ADVISOR);
        user.setStatus(AccountStatus.ACTIVE);
        user.setRegistrationDate(LocalDateTime.now());
        user.setPasswordHash("hashedPassword123");


        Advisor advisor = new Advisor();
        advisor.setId(id);
        advisor.setUser(user);
        advisor.setBio(bio);
        advisor.setExperienceYears(experienceYears);
        advisor.setAverageRating(rating);
        advisor.setProfileImageUrl(profileImageUrl);
        advisor.setSpecialties(specialties);

        return advisor;
    }

    private List<Advisor> createDiverseMockAdvisorList() {
        List<Advisor> advisors = new ArrayList<>();

        advisors.add(createMockAdvisor(1L, "John", "Smith",
                "Senior Financial Advisor with expertise in wealth management",
                8, 4.8, "http://example.com/john_smith.jpg",
                Set.of("Financial Planning", "Investment Advisory", "Retirement Planning")));
        advisors.add(createMockAdvisor(2L, "Emily", "Johnson",
                "Investment specialist focused on portfolio optimization",
                5, 4.6, "http://example.com/emily_johnson.jpg",
                Set.of("Stock Investment", "Portfolio Management")));
        return advisors;
    }


    @Test
    void should_return_all_advisors(){
        //prepare
        List<Advisor> advisor_list = createDiverseMockAdvisorList();

        //arrange
        when(advisorRepository.findAllByOrderByRatingDesc()).thenReturn(advisor_list);

        //act
        List<AdvisorProfileDto> result = advisorService.getAllAdvisors();

        //assert
        assertThat(result).hasSize(2);

        AdvisorProfileDto firstAdvisor = result.get(0);
        assertThat(firstAdvisor.getId()).isEqualTo(1L);
        assertThat(firstAdvisor.getFirstName()).isEqualTo("John");
        assertThat(firstAdvisor.getLastName()).isEqualTo("Smith");
        assertThat(firstAdvisor.getBio()).isEqualTo("Senior Financial Advisor with expertise in wealth management");
        assertThat(firstAdvisor.getExperienceYears()).isEqualTo(8);
        assertThat(firstAdvisor.getAverageRating()).isEqualTo(4.8);
        assertThat(firstAdvisor.getProfileImageUrl()).isEqualTo("http://example.com/john_smith.jpg");
        assertThat(firstAdvisor.getSpecialties()).contains("Financial Planning", "Investment Advisory", "Retirement Planning");

        // 验证第二个顾问的信息
        AdvisorProfileDto secondAdvisor = result.get(1);
        assertThat(secondAdvisor.getId()).isEqualTo(2L);
        assertThat(secondAdvisor.getFirstName()).isEqualTo("Emily");
        assertThat(secondAdvisor.getLastName()).isEqualTo("Johnson");
        assertThat(secondAdvisor.getBio()).isEqualTo("Investment specialist focused on portfolio optimization");
        assertThat(secondAdvisor.getExperienceYears()).isEqualTo(5);
        assertThat(secondAdvisor.getAverageRating()).isEqualTo(4.6);

        // 验证Repository方法被调用
        verify(advisorRepository).findAllByOrderByRatingDesc();

    }
}
