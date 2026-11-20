// SPDX-License-Identifier: GPL-3.0-or-later
package org.ubuntuquest.pocket;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.view.DisplayCutout;
import android.view.WindowInsets;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

/** Paint behind system bars while keeping desktop input inside their safe area. */
final class SystemBarFrame extends FrameLayout {
 private VrDragScroll vrDrag;private HomeReachability reach;private KeyboardViewport keyboard;private boolean followsKeyboard;
 void followKeyboard(boolean enabled){followsKeyboard=enabled;if(enabled&&keyboard==null)keyboard=new KeyboardViewport(this);if(!enabled&&keyboard!=null)keyboard.clear();requestApplyInsets();}
 void toggleReachability(){if(reach==null)reach=new HomeReachability(this);reach.toggle();}
 void restoreReachability(){if(reach!=null)reach.reset();}
 public boolean dispatchTouchEvent(android.view.MotionEvent event){if(reach!=null&&reach.outside(event))return true;if(keyboard!=null)keyboard.touch(event);if(vrDrag==null)vrDrag=new VrDragScroll(this);return vrDrag.dispatch(event,e->super.dispatchTouchEvent(e));}
 protected void onDetachedFromWindow(){if(vrDrag!=null)vrDrag.reset();restoreReachability();if(keyboard!=null)keyboard.clear();super.onDetachedFromWindow();}
 public void onWindowFocusChanged(boolean focused){if(!focused){if(vrDrag!=null)vrDrag.reset();restoreReachability();}super.onWindowFocusChanged(focused);}
 protected void onSizeChanged(int w,int h,int ow,int oh){super.onSizeChanged(w,h,ow,oh);if(w!=ow||h!=oh){restoreReachability();post(this::requestApplyInsets);}}
 /** Allow the window to fill cutout margins in either orientation. */
 static void allowCutout(Window window){
  if(Build.VERSION.SDK_INT>=28){WindowManager.LayoutParams p=window.getAttributes();p.layoutInDisplayCutoutMode=Build.VERSION.SDK_INT>=30?WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS:WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;window.setAttributes(p);}
 }
 SystemBarFrame(Context context){this(context,false);}
 SystemBarFrame(Context context,boolean extendLandscapeSides){this(context,extendLandscapeSides,false);}
 SystemBarFrame(Context context,boolean extendLandscapeSides,boolean followKeyboard){
  super(context);followsKeyboard=followKeyboard;if(followKeyboard)keyboard=new KeyboardViewport(this);
  setOnApplyWindowInsetsListener((view,insets)->{
   int left,top,right,bottom;
   boolean sideBleed=extendLandscapeSides&&getResources().getConfiguration().orientation==Configuration.ORIENTATION_LANDSCAPE;
   if(Build.VERSION.SDK_INT>=30){
    android.graphics.Insets safe=insets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout());
    left=safe.left;top=safe.top;right=safe.right;bottom=safe.bottom;
    if(sideBleed){android.graphics.Insets bars=insets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars());left=bars.left;right=bars.right;}
   }else{
    // Stable insets exclude the keyboard, preserving Test 14's overlay behavior.
    left=insets.getStableInsetLeft();top=insets.getStableInsetTop();right=insets.getStableInsetRight();bottom=insets.getStableInsetBottom();
    if(Build.VERSION.SDK_INT>=28){DisplayCutout cutout=insets.getDisplayCutout();if(cutout!=null){
     if(!sideBleed){left=Math.max(left,cutout.getSafeInsetLeft());right=Math.max(right,cutout.getSafeInsetRight());}top=Math.max(top,cutout.getSafeInsetTop());bottom=Math.max(bottom,cutout.getSafeInsetBottom());
    }}
   }
   if(followsKeyboard){
    int keyboardBottom=Build.VERSION.SDK_INT>=30?insets.getInsets(WindowInsets.Type.ime()).bottom:insets.getSystemWindowInsetBottom();
    boolean visible=Build.VERSION.SDK_INT>=30?insets.isVisible(WindowInsets.Type.ime()):keyboardBottom>bottom+Math.round(80*getResources().getDisplayMetrics().density);if(visible)restoreReachability();keyboard.update(visible);
    // Only subtract the IME where it intersects this pane (e.g. tabletop upper half).
    int[] location=new int[2];getLocationInWindow(location);
    int gap=Math.max(0,getRootView().getHeight()-location[1]-getHeight());
    bottom=Math.max(bottom,Math.max(0,keyboardBottom-gap));
   }
   setPadding(left,top,right,bottom);
   return insets;
  });
 }
}
