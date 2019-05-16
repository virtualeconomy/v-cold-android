package systems.v.coldwallet.Util;

import android.app.Application;

import java.util.ArrayList;

import systems.v.coldwallet.Wallet.Account;

public class SegQrcode extends Application {

    private String body;
    private String checkSum;
    private int curPage;
    private int totalPage;
    private ArrayList<Account> accounts = new ArrayList<>(0);

    @Override
    public void onCreate()
    {
        super.onCreate();
        setBody("");
        setCheckSum("");
        setCurPage(0);
        setTotalPage(0);
        setAccounts(accounts);
    }

    public void setBody(String body)
    {
        this.body = body;
    }

    public String getBody()
    {
        return body;
    }

    public void setCheckSum(String checkSum)
    {
       this.checkSum = checkSum;
    }

    public String getCheckSum()
    {
        return checkSum;
    }

    public void setCurPage(int curPage) {
        this.curPage = curPage;
    }

    public int getCurPage() {
        return curPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public ArrayList<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(ArrayList<Account> accounts) {
        this.accounts = accounts;
    }
}
