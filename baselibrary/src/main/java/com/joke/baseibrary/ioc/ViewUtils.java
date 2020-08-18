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
                        boolean isCheckNet = checkNet != null;
                        //4、动态的为方法注入事件
                        if (view != null) {
                            view.setOnClickListener(new DeclareOnClickListener(method, object, isCheckNet));
                        }
                    }
                }
            }
        }
    }

    private static class DeclareOnClickListener implements View.OnClickListener {
        private Method method;
        private Object handerType;
        private boolean isCheckNet;

        public DeclareOnClickListener(Method method, Object handlerType, boolean isCheckNetwork) {
            this.method = method;
            this.handerType = handlerType;
            this.isCheckNet = isCheckNetwork;
        }

        @Override
        public void onClick(View view) {
            //是否需要检测网络
            if (isCheckNet && !networkAvailable(view.getContext())) {
                Toast.makeText(view.getContext(), "网络不可用", Toast.LENGTH_SHORT).show();
                return;
            }
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
     * 检查网络是否可用
     *
     * @param context
     * @return
     */
    private static boolean networkAvailable(Context context) {
        boolean isAvailable = false;
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        if (info != null) {
            isAvailable = info.isAvailable();
        }

        return isAvailable;
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
