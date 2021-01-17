package top.dzurl.apptask.core.util.android.android.content.res;


import top.dzurl.apptask.core.util.android.android.util.AttributeSet;
import top.dzurl.apptask.core.util.android.v1.XmlPullParser;

public interface XmlResourceParser extends XmlPullParser, AttributeSet {
    void close();
}
