package root.integrat;

import java.util.concurrent.ConcurrentHashMap;

public abstract class AccountManager {

    private IServer server;
    private ConcurrentHashMap<String, Long> activeAccounts = new ConcurrentHashMap<>();
    public void init(IServer s) {
        server = s;
    }
    public AccountManagerResponse callLogin(String login, String password) {
        Long session = activeAccounts.get(login);
        if(session!=null)
            return AccountManagerResponse.ACCOUNT_MANAGER_RESPONSE;
        ServerResponse ret = server.login(login, makeSecure(password));
        switch(ret.code) {
            case ServerResponse.ALREADY_LOGGED:
                return AccountManagerResponse.ACCOUNT_MANAGER_RESPONSE;
            case ServerResponse.NO_USER_INCORRECT_PASSWORD:
                return AccountManagerResponse.NO_USER_INCORRECT_PASSWORD_RESPONSE;
            case ServerResponse.SUCCESS:{
                Object answ = ret.response;
                if(answ instanceof Long) {
                    activeAccounts.putIfAbsent(login, (long)ret.response);
                    return new AccountManagerResponse(AccountManagerResponse.SUCCEED, answ);
                }
                break;
            }
        }
        return new AccountManagerResponse(AccountManagerResponse.UNDEFINED_ERROR, ret);
    }
    public AccountManagerResponse callLogout(String user, long session) {
        Long rem = activeAccounts.remove(user);
        if(rem == null)
            return AccountManagerResponse.NOT_LOGGED_RESPONSE;
        ServerResponse resp = server.logout(session);
        switch(resp.code){
            case ServerResponse.NOT_LOGGED:
                return AccountManagerResponse.NOT_LOGGED_RESPONSE;
            case ServerResponse.SUCCESS:
                return AccountManagerResponse.SUCCEED_RESPONSE;
        }
        return new AccountManagerResponse(AccountManagerResponse.UNDEFINED_ERROR, resp);

    }
    public AccountManagerResponse withdraw(String login, long session, double amount){
        Long stored = activeAccounts.get(login);
        if(stored == null)
            return AccountManagerResponse.NOT_LOGGED_RESPONSE;
        if(stored!=session)
            return AccountManagerResponse.INCORRECT_SESSION_RESPONSE;
        ServerResponse resp = server.withdraw(session, amount);
        switch(resp.code){
            case ServerResponse.NOT_LOGGED:
                return AccountManagerResponse.NOT_LOGGED_RESPONSE;
            case ServerResponse.NO_MONEY:
                Object r = resp.response;
                if(r!=null && r instanceof Double)
                    return new AccountManagerResponse(AccountManagerResponse.NO_MONEY, (Double)r);
                break;
            case ServerResponse.SUCCESS:
                r = resp.response;
                if(r!=null && r instanceof Double)
                    return new AccountManagerResponse(AccountManagerResponse.SUCCEED, (Double)r);
                break;
        }
        return new AccountManagerResponse(AccountManagerResponse.UNDEFINED_ERROR, resp);
    }
    public AccountManagerResponse deposit(String login, long session, double amount){
        Long stored = activeAccounts.get(login);
        if(stored == null)
            return AccountManagerResponse.NOT_LOGGED_RESPONSE;
        if(stored!=session)
            return AccountManagerResponse.INCORRECT_SESSION_RESPONSE;
        ServerResponse resp = server.deposit(session, amount);
        switch(resp.code){
            case ServerResponse.NOT_LOGGED:
                return AccountManagerResponse.NOT_LOGGED_RESPONSE;
            case ServerResponse.SUCCESS:
                Object r = resp.response;
                r = resp.response;
                if(r!=null && r instanceof Double)
                    return new AccountManagerResponse(AccountManagerResponse.SUCCEED, (Double)r);
                break;
        }
        return new AccountManagerResponse(AccountManagerResponse.UNDEFINED_ERROR, resp);
    }
    public AccountManagerResponse getBalance(String login, long session){
        Long stored = activeAccounts.get(login);
        if(stored == null)
            return AccountManagerResponse.NOT_LOGGED_RESPONSE;
        if(stored!=session)
            return AccountManagerResponse.INCORRECT_SESSION_RESPONSE;
        ServerResponse resp = server.getBalance(session);
        switch(resp.code){
            case ServerResponse.NOT_LOGGED:
                return AccountManagerResponse.NOT_LOGGED_RESPONSE;
            case ServerResponse.SUCCESS:
                Object r = resp.response;
                if(r!=null && r instanceof Double)
                    return new AccountManagerResponse(AccountManagerResponse.SUCCEED, (Double)r);
                break;
        }
        return new AccountManagerResponse(AccountManagerResponse.UNDEFINED_ERROR, resp);
    }
    protected abstract String makeSecure(String password);
}
