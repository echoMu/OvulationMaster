package com.example.echomu.ovulationmaster.ovulation;

import android.content.Context;

/**
 * author : echoMu
 * date   : 2019/3/26
 * desc   :
 */
public class PublicMethod {
    /**
     * dip to px
     *
     * @param context
     * @param dipValue
     * @return
     */
    public static int dip2px(Context context, float dipValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

}
