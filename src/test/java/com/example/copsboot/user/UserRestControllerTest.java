package com.example.copsboot.user;

import com.example.copsboot.infrastructure.SpringProfiles;
import com.example.copsboot.infrastructure.security.AuthorizationServerConfiguration;
import com.example.copsboot.infrastructure.security.ResourceServerConfiguration;
import com.example.copsboot.infrastructure.security.SecurityConfiguration;
import com.example.copsboot.infrastructure.security.StubUserDetailsService;

import com.example.copsboot.user.web.UserRestController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static com.example.copsboot.infrastructure.security.SecurityHelperForMockMvc.HEADER_AUTHORIZATION;
import static com.example.copsboot.infrastructure.security.SecurityHelperForMockMvc.bearer;
import static com.example.copsboot.infrastructure.security.SecurityHelperForMockMvc.obtainAccessToken;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(UserRestController.class)
@ActiveProfiles(SpringProfiles.TEST)
public class UserRestControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserService service;

    @TestConfiguration
    @Import({ResourceServerConfiguration.class, AuthorizationServerConfiguration.class})
    static class TestConfig {
        @Bean
        public UserDetailsService userDetailsService() {
            return new StubUserDetailsService();
        }

        @Bean
        public TokenStore getTokenStore() {
            return new InMemoryTokenStore();
        }

        @Bean
        public SecurityConfiguration securityConfiguration() {
            return new SecurityConfiguration();
        }
    }

    @Test
    public void givenNotAuthenticated_whenAskingMyDetails_forbidden() throws Exception {
        mvc.perform(get("/api/users/me"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void givenAuthenticatedAsOfficer_whenAskingMyDetails_detailsReturned() throws Exception {

        when(service.getUser(Users.officer().getId())).thenReturn(Optional.of(Users.officer()));

        String accessToken = obtainAccessToken(mvc, Users.OFFICER_EMAIL, Users.OFFICER_PASSWORD);

        mvc.perform(get("/api/users/me")
                .header(HEADER_AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").value(Users.OFFICER_EMAIL))
                .andExpect(jsonPath("roles").isArray())
                .andExpect(jsonPath("roles[0]").value("OFFICER"));
    }
}
