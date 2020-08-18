package com.joke.baseibrary.ioc;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by BG360106 on 2020/8/17.
 * Description:
 */
public class ViewUtils {
    public static void inject(Activity activity) {
        inject(new ViewFinder(activity), activity);
    }

    public static void inject(View view) {
        inject(new ViewFinder(view), view);
    }

    public static void inject(View view, Object object) {
        inject(new ViewFinder(view), object);
    }

    private static void inject(ViewFinder finder, Object object) {
        //注入属性
        injectFiled(finder, object);
        //注入方法
        injectEvent(finder, object);
    }

    private static void injectNetWork(ViewFinder finder, Object object) {
        //获取class
        Class<?> clazz = object.getClass();
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {

        }
    }

    private static void injectEvent(ViewFinder finder, Object object) {
        //1.获取class
        Class<?> clazz = object.getClass();
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            //2.通过注解获取属性上面的ViewById的值
            OnClick onClick = method.getAnnotation(OnClick.class);
            if (onClick != null) {
                int[] viewIds = onClick.value();
                if (viewIds.length > 0) {
                    for (int viewId : viewIds) {
                        // 3.遍历所有的id 先findViewById然后 setOnClickListener
                        View view = finder.findViewById(viewId);
                        //获取注解位置
                        CheckNet checkNet = method.getAnnotation(CheckNet.class);
                        Context context = null;
                        if (checkNet != null) {
                            if(object instanceof View) context = ((View) object).getContext();
                            if(object instanceof Activity) context = (Context) object;
                            if(context == null){
                                throw new RuntimeException("Context is null");
                            }
                        }
                        if (view != null && context!=null && isNetworkAvailable(context)) {
                            view.setOnClickListener(new DeclareOnClickListener(method, object));
                        }
                    }
                }
            }
        }
    }

    private static class DeclareOnClickListener implements View.OnClickListener {
        private Method method;
        private Object handerType;

        public DeclareOnClickListener(Method method, Object handlerType) {
            this.method = method;
            this.handerType = handlerType;
        }

        @Override
        public void onClick(View view) {
            //调用反射的方法
            method.setAccessible(true);
            try {
                method.invoke(handerType, view);
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    method.invoke(handerType, null);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    /**
     * 检查网络是否可用 思考
     *
     * @param context
     * @return
     */
    public static boolean isNetworkAvailable(Context context) {
        boolean isNetwork = true;
        ConnectivityManager manager = (ConnectivityManager) context
                .getApplicationContext().getSystemService(
                        Context.CONNECTIVITY_SERVICE);
        if (manager == null) {
            isNetwork = false;
        }
        NetworkInfo networkinfo = manager.getActiveNetworkInfo();
        if (networkinfo == null || !networkinfo.isAvailable()) {
            isNetwork = false;
        }
        if(!isNetwork) {
            //toast
            Toast.makeText(context, "亲，当前无网络哦", Toast.LENGTH_LONG).show();
        }
        return isNetwork;
    }

    private static void injectFiled(ViewFinder finder, Object object) {
        //1.获取所有的属性
        Class<?> mClass = object.getClass();
        // 获取所有属性包括私有和公有
        Field[] fields = mClass.getDeclaredFields();
        for (Field field : fields) {
            // 2. 通过注解获取属性上面ViewById的值
            ViewById viewById = field.getAnnotation(ViewById.class);
            if (viewById != null) {
                // 获取ViewById属性上的viewId值
                int viewId = viewById.value();
                //3.调用viewFinder
                View view = finder.findViewById(viewId);
                if (view != null) {
                    // 4. 反射注入View属性
                    // 设置所有属性都能注入包括私有和公有
                    field.setAccessible(true);
                    try {
                        field.set(object, view);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                } else {
                    throw new RuntimeException("Invalid @ViewInject for" + mClass.getSimpleName() + "." + field.getName());
                }
            }
        }
    }
}
