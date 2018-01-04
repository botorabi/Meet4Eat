package net.m4e.app.user;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.revinate.assertj.json.JsonPathAssert;
import net.m4e.app.auth.AuthorityConfig;
import net.m4e.app.resources.StatusEntity;
import net.m4e.common.ResponseResults;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

/**
 * @author ybroeker
 */
class UserAuthenticationFacadeRESTTest {

    private final static String EXISTING_USER = "testuser";
    private final static String NON_EXISTING_USER = "nonexisting";
    private final static String PASSWORD = "password";

    private final static String SESSION_ID = "session_id";

    private final static String RIGHT_CREDENTIALS = String.format("{\"login\":\"%s\", \"password\":\"%s\"}", EXISTING_USER, clientSideHash(PASSWORD, SESSION_ID));

    @Mock
    Users users;
    @Mock
    HttpServletRequest request;
    @Mock
    HttpSession session;

    UserAuthenticationFacadeREST userAuthentication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(session.getId()).thenReturn(SESSION_ID);

        Mockito.when(request.getSession()).thenReturn(session);
        userAuthentication = new UserAuthenticationFacadeREST(users);

        UserEntity userEntity = new UserEntity();
        userEntity.setStatus(new StatusEntity());
        userEntity.setPassword(AuthorityConfig.getInstance().createPassword(PASSWORD));
        userEntity.setLogin(EXISTING_USER);
        userEntity.setId(1L);
        Mockito.when(users.findUser(EXISTING_USER)).thenReturn(userEntity);
        Mockito.when(users.findUser(NON_EXISTING_USER)).thenReturn(null);
    }

    private static String clientSideHash(String plainPassword, String salt) {
        return AuthorityConfig.getInstance().createPassword(
                AuthorityConfig.getInstance().createPassword(plainPassword) + salt);
    }


    @NotNull
    private JsonPathAssert assertThat(@Nullable String string) {
        if (string == null) {
            string = "";
        }
        DocumentContext ctx = JsonPath.parse(string);
        return com.revinate.assertj.json.JsonPathAssert.assertThat(ctx);
    }


    @Test
    void login_rightCredentials() {
        String input = RIGHT_CREDENTIALS;


        String response = userAuthentication.login(input, request);


        assertThat(response).jsonPathAsString("$.code").isEqualTo(Integer.toString(ResponseResults.CODE_OK));
    }

    @Test
    void login_wrongCredentials() {
        String input = "{\"login\":\"testuser\", \"password\":\"" + clientSideHash("wrong", "salt") + "\"}";


        String response = userAuthentication.login(input, request);


        assertThat(response).jsonPathAsString("$.code").isEqualTo(Integer.toString(ResponseResults.CODE_UNAUTHORIZED));
    }

    @Test
    void login_alreadyLoggedInUser() {
        String input = RIGHT_CREDENTIALS;
        Mockito.when(session.getAttribute(AuthorityConfig.SESSION_ATTR_USER)).thenReturn(new Object());

        String response = userAuthentication.login(input, request);


        assertThat(response).jsonPathAsString("$.code").isEqualTo(Integer.toString(ResponseResults.CODE_NOT_ACCEPTABLE));
    }

    @Test
    void login_nonExistingUser() {
        String input = "{\"login\":\"nonexisting\", \"password\":\"password\"}";


        String response = userAuthentication.login(input, request);


        assertThat(response).jsonPathAsString("$.code").isEqualTo(Integer.toString(ResponseResults.CODE_NOT_FOUND));
    }

    @Test
    void login_invalidInput() {
        String input = "{\"password\":\"password\"}";


        String response = userAuthentication.login(input, request);


        assertThat(response).jsonPathAsString("$.code").isEqualTo(Integer.toString(ResponseResults.CODE_BAD_REQUEST));
    }

    @Test
    void login_emptyInput() {
        String input = "";


        String response = userAuthentication.login(input, request);


        assertThat(response).jsonPathAsString("$.code").isEqualTo(Integer.toString(ResponseResults.CODE_BAD_REQUEST));
    }


}
