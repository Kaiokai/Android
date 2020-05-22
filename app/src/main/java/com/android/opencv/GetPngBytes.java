package com.android.opencv;

public class GetPngBytes {
    String flac_start = "|start|";
    String flac_end = "|end|";

    // 接收socketDemo类getServerBytes获取的Bytes类型png 处理后返回
    public String getPicStr(String str) {
        String fstr = str.substring(str.indexOf(flac_start)+7,str.indexOf(flac_end));
        return fstr;
    }
    // 获取一次最终数组剩下的字符
    public String getStrOther(String str) {
        // 判断 |end| 后面还有没有字符串
        if(str.indexOf(flac_end)+4 < str.length()-1) {
            // 将|end|后面的部分取出
            String strs = str.substring(str.indexOf(flac_end)+5);
            // end不在最后--查看剩下的是否有|start||end|
            if (strs.indexOf(flac_start) != -1) {
                if(strs.indexOf(flac_end) != -1) {
                    // 后面有头也有尾部 先不写...不太可能一次传两张吧 可能就不显示
                    return "";
                }
                // 只有|start| 将剩下的返回
                return strs;
            }else {
                // start 都没有
                return "";
            }
        }else {
            // end在最后
            return "";
        }
    }
    // 判断接受的str是不是一次最终数组
    public boolean isFinalArr(String str) {
        // 判断是否存在尾部
        if (str.indexOf("|end|") != -1) {
            // 存在--为一次最终函数
            return true;
        }else {
            return false;
        }
    }
}
