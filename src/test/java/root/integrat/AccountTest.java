package root.integrat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

//@RunWith(MockitoJUnitRunner.class)
class AccountTest {

    private IServer server;
    private AccountManager manager;

    @BeforeEach
    void setUp() {
        server = mock(IServer.class);
        manager = spy(new AccountManager() {
            @Override
            protected String makeSecure(String password) {
                return password;
            }
        });
        manager.init(server);
    }

    @Test
    void loginTest() {
        long session_num = 3;
        when(server.login(anyString(), anyString())).thenReturn(
                new ServerResponse(ServerResponse.SUCCESS, session_num));
        var response = manager.callLogin("name", "pass");
        assertEquals(AccountManagerResponse.SUCCEED, response.code);
        assertEquals(session_num, (long)(response.response));
    }

    @Test
    void alreadyLoggedServerTest() {
        long session_num = 3;
        when(server.login("name", "pass")).thenReturn(
                new ServerResponse(ServerResponse.SUCCESS, session_num));
        when(server.login("same_name", "pass")).thenReturn(
                new ServerResponse(ServerResponse.ALREADY_LOGGED, session_num));
        manager.callLogin("name", "pass");
        var res = manager.callLogin("same_name", "pass");
        assertEquals(AccountManagerResponse.ALREADY_LOGGED, res.code);

    }

    @Test
    void alreadyLoggedManagerTest() {
        long session_num = 3;
        when(server.login("name", "pass")).thenReturn(
                new ServerResponse(ServerResponse.SUCCESS, session_num));
        manager.callLogin("name", "pass");
        var res = manager.callLogin("name", "pass");
        assertEquals(AccountManagerResponse.ALREADY_LOGGED, res.code);

    }

    @Test
    void wrongPasswordOrNameTest() {
        long session_num = 3;
        when(server.login("wrong_name", "wrong_pass")).thenReturn(
                new ServerResponse(ServerResponse.NO_USER_INCORRECT_PASSWORD, session_num));
        var res = manager.callLogin("wrong_name", "wrong_pass");
        assertEquals(AccountManagerResponse.NO_USER_INCORRECT_PASSWORD, res.code);
    }

    @Test
    void undefinedErrorTest() {
        long session_num = 3;
        when(server.login("wrong_name", "wrong_pass")).thenReturn(
                new ServerResponse(ServerResponse.UNDEFINED_ERROR, session_num));
        var res = manager.callLogin("wrong_name", "wrong_pass");
        assertEquals(AccountManagerResponse.UNDEFINED_ERROR, res.code);
    }

    @Test
    void logOutTest() {
        long session_num = 3;
        when(server.login("name", "pass")).thenReturn(
                new ServerResponse(ServerResponse.SUCCESS, session_num));
        when(server.logout(session_num)).thenReturn(
                new ServerResponse(ServerResponse.SUCCESS, session_num));
        manager.callLogin("name", "pass");
        var res = manager.callLogout("name", session_num);
        assertEquals(AccountManagerResponse.SUCCEED, res.code);
    }

    @Test
    void logOutNotLoggedServerTest() {
        long session_num = 3;
        when(server.login("name", "pass")).thenReturn(
                new ServerResponse(ServerResponse.SUCCESS, session_num));
        when(server.logout(session_num)).thenReturn(
                new ServerResponse(ServerResponse.NOT_LOGGED, session_num));
        manager.callLogin("name", "pass");
        var res = manager.callLogout("name", session_num);
        assertEquals(AccountManagerResponse.NOT_LOGGED, res.code);
    }

    @Test
    void logOutNotLoggedManagerTest() {
        long session_num = 3;
        when(server.logout(session_num)).thenReturn(
                new ServerResponse(ServerResponse.SUCCESS, session_num));
        var res = manager.callLogout("name", session_num);
        assertEquals(AccountManagerResponse.NOT_LOGGED, res.code);
    }

    @Test
    void logoutErrorTest() {
        long session_num = 3;
        when(server.login("name", "pass")).thenReturn(
                new ServerResponse(ServerResponse.SUCCESS, session_num));
        when(server.logout(session_num)).thenReturn(
                new ServerResponse(ServerResponse.UNDEFINED_ERROR, session_num));
        manager.callLogin("name", "pass");
        var res = manager.callLogout("name", session_num);
        assertEquals(AccountManagerResponse.UNDEFINED_ERROR, res.code);
    }

    @Test
    void depositTest() {
        long session_num = 3;
        when(server.login("name", "pass")).thenReturn(
                new ServerResponse(ServerResponse.SUCCESS, session_num));
        when(server.deposit(anyLong(), anyDouble())).thenReturn(
                new ServerResponse(ServerResponse.SUCCESS, (double)10));
        manager.callLogin("name", "pass");
        var res = manager.deposit("name", (long)session_num, (double) 124);
        assertEquals(AccountManagerResponse.SUCCEED, res.code);
    }

    @Test
    void depositNotLoggedServerTest() {
        long session_num = 3;
        when(server.login("name", "pass")).thenReturn(
                new ServerResponse(ServerResponse.SUCCESS, session_num));
        when(server.deposit(anyLong(), anyDouble())).thenReturn(
                new ServerResponse(ServerResponse.NOT_LOGGED, (double)10));
        manager.callLogin("name", "pass");
        var res = manager.deposit("name", (long)session_num, (double) 124);
        assertEquals(AccountManagerResponse.NOT_LOGGED, res.code);
    }

    @Test
    void depositNotLoggedManagerTest() {
        long session_num = 3;
        when(server.login("name", "pass")).thenReturn(
                new ServerResponse(ServerResponse.SUCCESS, session_num));
        when(server.withdraw(anyLong(), anyDouble())).thenReturn(
                new ServerResponse(ServerResponse.SUCCESS, (double)10));
        var res = manager.withdraw("name", (long)session_num, (double) 124);
        assertEquals(AccountManagerResponse.NOT_LOGGED, res.code);
    }

    @Test
    void depositWrongSessionTest() {
        long session_num = 3;
        when(server.login("name", "pass")).thenReturn(
                new ServerResponse(ServerResponse.SUCCESS, session_num));
        when(server.deposit(anyLong(), anyDouble())).thenReturn(
                new ServerResponse(ServerResponse.SUCCESS, (double)10));
        manager.callLogin("name", "pass");
        var res = manager.deposit("name", (long)session_num + 1, (double) 124);
        assertEquals(AccountManagerResponse.INCORRECT_SESSION, res.code);
    }

    @Test
    void depositNoMoneyTest() {
        long session_num = 3;
        when(server.login("name", "pass")).thenReturn(
                new ServerResponse(ServerResponse.SUCCESS, session_num));
        when(server.deposit(anyLong(), anyDouble())).thenReturn(
                new ServerResponse(ServerResponse.NO_MONEY, (double)10));
        manager.callLogin("name", "pass");
        var res = manager.deposit("name", (long)session_num, (double) 124);
        assertEquals(AccountManagerResponse.UNDEFINED_ERROR, res.code);
    }

    @Test
    void depositErrorTest() {
        long session_num = 3;
        when(server.login("name", "pass")).thenReturn(
                new ServerResponse(ServerResponse.SUCCESS, session_num));
        when(server.deposit(anyLong(), anyDouble())).thenReturn(
                new ServerResponse(ServerResponse.UNDEFINED_ERROR, (double)10));
        manager.callLogin("name", "pass");
        var res = manager.deposit("name", (long)session_num, (double) 124);
        assertEquals(AccountManagerResponse.UNDEFINED_ERROR, res.code);
    }


    @Test
    void withdrawTest() {
        long session_num = 3;
        when(server.login("name", "pass")).thenReturn(
                new ServerResponse(ServerResponse.SUCCESS, session_num));
        when(server.withdraw(anyLong(), anyDouble())).thenReturn(
                new ServerResponse(ServerResponse.SUCCESS, (double)10));
        manager.callLogin("name", "pass");
        var res = manager.withdraw("name", (long)session_num, (double) 124);
        assertEquals(AccountManagerResponse.SUCCEED, res.code);
    }

    @Test
    void withdrawNotLoggedServerTest() {
        long session_num = 3;
        when(server.login("name", "pass")).thenReturn(
                new ServerResponse(ServerResponse.SUCCESS, session_num));
        when(server.withdraw(anyLong(), anyDouble())).thenReturn(
                new ServerResponse(ServerResponse.NOT_LOGGED, (double)10));
        manager.callLogin("name", "pass");
        var res = manager.withdraw("name", (long)session_num, (double) 124);
        assertEquals(AccountManagerResponse.NOT_LOGGED, res.code);
    }

    @Test
    void withdrawNotLoggedManagerTest() {
        long session_num = 3;
        when(server.login("name", "pass")).thenReturn(
                new ServerResponse(ServerResponse.SUCCESS, session_num));
        when(server.withdraw(anyLong(), anyDouble())).thenReturn(
                new ServerResponse(ServerResponse.SUCCESS, (double)10));
        var res = manager.withdraw("name", (long)session_num, (double) 124);
        assertEquals(AccountManagerResponse.NOT_LOGGED, res.code);
    }

    @Test
    void withdrawWrongSessionTest() {
        long session_num = 3;
        when(server.login("name", "pass")).thenReturn(
                new ServerResponse(ServerResponse.SUCCESS, session_num));
        when(server.withdraw(anyLong(), anyDouble())).thenReturn(
                new ServerResponse(ServerResponse.SUCCESS, (double)10));
        manager.callLogin("name", "pass");
        var res = manager.withdraw("name", (long)session_num + 1, (double) 124);
        assertEquals(AccountManagerResponse.INCORRECT_SESSION, res.code);
    }

    @Test
    void withdrawNoMoneyTest() {
        long session_num = 3;
        when(server.login("name", "pass")).thenReturn(
                new ServerResponse(ServerResponse.SUCCESS, session_num));
        when(server.withdraw(anyLong(), anyDouble())).thenReturn(
                new ServerResponse(ServerResponse.NO_MONEY, (double)10));
        manager.callLogin("name", "pass");
        var res = manager.withdraw("name", (long)session_num, (double) 124);
        assertEquals(AccountManagerResponse.NO_MONEY, res.code);
    }

    @Test
    void withdrawErrorTest() {
        long session_num = 3;
        when(server.login("name", "pass")).thenReturn(
                new ServerResponse(ServerResponse.SUCCESS, session_num));
        when(server.withdraw(anyLong(), anyDouble())).thenReturn(
                new ServerResponse(ServerResponse.UNDEFINED_ERROR, (double)10));
        manager.callLogin("name", "pass");
        var res = manager.withdraw("name", (long)session_num, (double) 124);
        assertEquals(AccountManagerResponse.UNDEFINED_ERROR, res.code);
    }



    @Test
    void getBalanceTest() {
        long session_num = 3;
        when(server.login("name", "pass")).thenReturn(
                new ServerResponse(ServerResponse.SUCCESS, session_num));
        when(server.getBalance(session_num)).thenReturn(
                new ServerResponse(ServerResponse.SUCCESS, (double)10));
        manager.callLogin("name", "pass");
        var res = manager.getBalance("name", (long)session_num);
        assertEquals(AccountManagerResponse.SUCCEED, res.code);
    }

    @Test
    void getBalanceNotLoggedServerTest() {
        long session_num = 3;
        when(server.login("name", "pass")).thenReturn(
                new ServerResponse(ServerResponse.SUCCESS, session_num));
        when(server.getBalance(session_num)).thenReturn(
                new ServerResponse(ServerResponse.NOT_LOGGED, (double)10));
        manager.callLogin("name", "pass");
        var res = manager.getBalance("name", (long)session_num);
        assertEquals(AccountManagerResponse.NOT_LOGGED, res.code);
    }

    @Test
    void getBalanceNotLoggedManagerTest() {
        long session_num = 3;
        when(server.login("name", "pass")).thenReturn(
                new ServerResponse(ServerResponse.SUCCESS, session_num));
        when(server.getBalance(session_num)).thenReturn(
                new ServerResponse(ServerResponse.SUCCESS, (double)10));
        var res = manager.getBalance("name", (long)session_num);
        assertEquals(AccountManagerResponse.NOT_LOGGED, res.code);
    }

    @Test
    void getBalanceWrongSessionTest() {
        long session_num = 3;
        when(server.login("name", "pass")).thenReturn(
                new ServerResponse(ServerResponse.SUCCESS, session_num));
        when(server.getBalance(session_num)).thenReturn(
                new ServerResponse(ServerResponse.SUCCESS, (double)10));
        manager.callLogin("name", "pass");
        var res = manager.getBalance("name", (long)session_num + 1);
        assertEquals(AccountManagerResponse.INCORRECT_SESSION, res.code);
    }

    @Test
    void getBalanceErrorTest() {
        long session_num = 3;
        when(server.login("name", "pass")).thenReturn(
                new ServerResponse(ServerResponse.SUCCESS, session_num));
        when(server.getBalance(session_num)).thenReturn(
                new ServerResponse(ServerResponse.UNDEFINED_ERROR, (double)10));
        manager.callLogin("name", "pass");
        var res = manager.getBalance("name", (long)session_num);
        assertEquals(AccountManagerResponse.UNDEFINED_ERROR, res.code);
    }



    @Test
    void scenarioOneTest() {
        long session_num = 3;
        when(server.login("name", "pass")).thenReturn(
                new ServerResponse(ServerResponse.SUCCESS, session_num));
        when(server.login("name", "wrong_pass")).thenReturn(
                new ServerResponse(ServerResponse.NO_USER_INCORRECT_PASSWORD, session_num));
        when(server.login("wrong_name", "pass")).thenReturn(
                new ServerResponse(ServerResponse.NO_USER_INCORRECT_PASSWORD, session_num));
        when(server.getBalance(session_num)).thenReturn(
                new ServerResponse(ServerResponse.SUCCESS, (double)0));
        when(server.deposit(anyLong(), anyDouble())).thenReturn(
                new ServerResponse(ServerResponse.SUCCESS, (double)100));
        assertEquals(AccountManagerResponse.NO_USER_INCORRECT_PASSWORD, manager.callLogin("name", "wrong_pass").code);
        assertEquals(AccountManagerResponse.NO_USER_INCORRECT_PASSWORD, manager.callLogin("wrong_name", "pass").code);
        assertEquals(AccountManagerResponse.SUCCEED, manager.callLogin("name", "pass").code);
        var res = manager.getBalance("name", (long)session_num);
        assertEquals(AccountManagerResponse.SUCCEED, res.code);
        assertEquals((double)0, res.response);
        res = manager.deposit("name", (long)session_num, 100);
        assertEquals(AccountManagerResponse.SUCCEED, res.code);
        assertEquals((double)100, res.response);
    }

    @Test
    void scenarioTwoTest() {
        long session_num = 3;
        when(server.login("name", "pass")).thenReturn(
                new ServerResponse(ServerResponse.SUCCESS, session_num));
        when(server.getBalance(session_num)).thenReturn(
                new ServerResponse(ServerResponse.SUCCESS, (double)0));
        when(server.deposit(anyLong(), anyDouble())).thenReturn(
                new ServerResponse(ServerResponse.SUCCESS, (double)100));
        when(server.withdraw(session_num, 50)).thenReturn(
                new ServerResponse(ServerResponse.NO_MONEY, (double)0), new ServerResponse(ServerResponse.SUCCESS, (double)50));
        assertEquals(AccountManagerResponse.SUCCEED, manager.callLogin("name", "pass").code);
        assertEquals(AccountManagerResponse.NO_MONEY, manager.withdraw("name", session_num, (double) 50).code);
        var res = manager.getBalance("name", (long)session_num);
        assertEquals(AccountManagerResponse.SUCCEED, res.code);
        assertEquals((double)0, res.response);
        res = manager.deposit("name", (long)session_num, 100);
        assertEquals(AccountManagerResponse.SUCCEED, res.code);
        assertEquals((double)100, res.response);
        assertEquals(AccountManagerResponse.INCORRECT_SESSION, manager.withdraw("name", (long)session_num + 1, (double)50).code);
        assertEquals(AccountManagerResponse.SUCCEED, manager.withdraw("name", (long)session_num, (double)50).code);
    }
}