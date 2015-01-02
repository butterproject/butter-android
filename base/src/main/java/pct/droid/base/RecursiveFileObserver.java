package pct.droid.base;

import android.os.FileObserver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class RecursiveFileObserver extends FileObserver
{

    public static final int CHANGES_ONLY = CREATE | DELETE | CLOSE_WRITE | MOVE_SELF | MOVED_FROM | MOVED_TO;

    List<SingleFileObserver> mObservers;
    String mPath;
    int mMask;
    public String directoryPath;

    public RecursiveFileObserver(String path) {
        this(path, ALL_EVENTS);
        directoryPath = path;
    }

    public RecursiveFileObserver(String path, int mask) {
        super(path, mask);
        mPath = path;
        mMask = mask;
    }

    @Override
    public void startWatching() {
        if (mObservers != null) return;

        mObservers = new ArrayList<SingleFileObserver>();
        Stack<String> stack = new Stack<String>();
        stack.push(mPath);

        while (!stack.isEmpty()) {
            String parent = stack.pop();
            mObservers.add(new SingleFileObserver(parent, mMask));
            File path = new File(parent);
            File[] files = path.listFiles();
            if (null == files) continue;
            for (File f : files)
            {
                if (f.isDirectory() && !f.getName().equals(".") && !f.getName().equals("..")) {
                    stack.push(f.getPath());
                }
            }
        }

        for (SingleFileObserver sfo : mObservers) {
            sfo.startWatching();
        }
    }

    @Override
    public void stopWatching() {
        if (mObservers == null) return;

        for (SingleFileObserver sfo : mObservers) {
            sfo.stopWatching();
        }
        mObservers.clear();
        mObservers = null;
    }

    @Override
    public void onEvent(int event, String filePath) 
    {
        event &= FileObserver.ALL_EVENTS;


        synchronized (this)
        {
            //Log.i("FileManager", "event occured:"+filePath);

            if (event == FileObserver.CREATE || event == FileObserver.MOVED_TO) 
            {

                return;
            }

            if (event == FileObserver.DELETE  || event == FileObserver.MOVED_FROM) 
            {

                return;
            }
        }
    }


    class SingleFileObserver extends FileObserver 
    {
        String mPath;

        public SingleFileObserver(String path) {
            this(path, ALL_EVENTS);
            mPath = path;
        }

        public SingleFileObserver(String path, int mask) {
            super(path, mask);
            mPath = path;
        }

        @Override
        public void onEvent(int event, String path) {
            String newPath = mPath + "/" + path;
            RecursiveFileObserver.this.onEvent(event, newPath);
        }
    }
}