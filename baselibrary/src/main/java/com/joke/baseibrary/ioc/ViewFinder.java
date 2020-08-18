package com.joke.baseibrary.ioc;

import android.app.Activity;
import android.view.View;

/**
 * Created by BG360106 on 2020/8/17.
 * Description:
 */
public class ViewFinder {
    private View mView;
    private Activity mActivity;

    public ViewFinder(View view) {
        this.mView = view;
    }

    public ViewFinder(Activity activity) {
        this.mActivity = activity;
    }

    public View findViewById(int id){
      return mActivity != null?mActivity.findViewById(id):mView.findViewById(id);
    }
}
