package com.nauman.vdownloader.Helper;

import android.annotation.SuppressLint;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.util.Log;

import java.lang.reflect.Field;

/**
 * Created by Nauman Shafqat on 6/02/2018.
 */

public class BottomNavigationViewHelper {
    @SuppressLint("RestrictedApi")
    public static void disableShiftMode(BottomNavigationView view) {
        BottomNavigationMenuView mView = (BottomNavigationMenuView) view.getChildAt(0);
        try {
            Field shiftMode = mView.getClass().getDeclaredField("mShiftingMode");
            shiftMode.setAccessible(true);
            shiftMode.setBoolean(mView, false);
            shiftMode.setAccessible(false);
            for (int i = 0; i < mView.getChildCount(); i++) {
                BottomNavigationItemView mItems = (BottomNavigationItemView) mView.getChildAt(i);
                mItems.setShiftingMode(false);
                mItems.setChecked(mItems.getItemData().isChecked());
            }
        } catch (NoSuchFieldException er) {
            Log.i("Error", "Shift Mode Fields Not Available",er);
        } catch (IllegalAccessException e) {
            Log.i("Error", "Shift Mode Fields Change Error",e);
        }
    }
}