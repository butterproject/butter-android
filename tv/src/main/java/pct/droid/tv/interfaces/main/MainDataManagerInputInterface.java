package pct.droid.tv.interfaces.main;

public interface MainDataManagerInputInterface
{
    public abstract void getList(String genre, int page, MainDataManagerCallback callback);
}
