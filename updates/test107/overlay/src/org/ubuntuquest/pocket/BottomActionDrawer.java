// SPDX-License-Identifier: GPL-3.0-or-later
package org.ubuntuquest.pocket;
import android.app.Activity;
import android.graphics.drawable.*;
import android.view.*;
import android.widget.*;
import android.view.accessibility.AccessibilityManager;

/** A visible, keyboard-accessible handle inside the app's safe area. */
final class BottomActionDrawer {
 private static final String TAG="bottom-action-handle";
 static void install(Activity owner,Runnable open){install(owner,open,true);}
 static void install(Activity owner,Runnable open,boolean visible){
  FrameLayout content=owner.findViewById(android.R.id.content);
  if(content==null||content.getChildCount()!=1)return;
  View root=content.getChildAt(0);
  if(root instanceof FoldSafeFrame&&((FoldSafeFrame)root).getChildCount()==1)root=((FoldSafeFrame)root).getChildAt(0);
  if(!(root instanceof SystemBarFrame))return;
  SystemBarFrame frame=(SystemBarFrame)root;
  boolean enabled=visible&&DeviceProfiles.settings(owner).getBoolean("bottom-actions",true);View existing=frame.findViewWithTag(TAG);SettingsUi ui=new SettingsUi(owner);
  if(!enabled){if(existing!=null)frame.removeView(existing);return;}
  if(existing!=null)frame.removeView(existing);
  Button handle=ui.button("⌃",null,open);handle.setTag(TAG);handle.setContentDescription("Open actions. Tap or swipe up.");
  // Overlay only: never change the desktop's measured size or reserve a bottom band.
  int target=VrProfile.target(owner,48),height=VrProfile.active(owner)?32:24;
  handle.setMinHeight(0);handle.setMinimumHeight(0);handle.setPadding(0,0,0,0);
  handle.setTextSize(22);handle.setStateListAnimator(null);handle.setElevation(0);
  handle.setTooltipText("Actions");
  ThemeColors colors=ThemeColors.read(owner,"desktop");
  int fill=ThemeColors.transparent(colors.button,ThemeColors.transparency(owner));
  int active=ThemeColors.blend(colors.accent,colors.panel,.38f);
  handle.setTextColor(colors.border);
  StateListDrawable background=new StateListDrawable();
  background.addState(new int[]{android.R.attr.state_pressed},pill(ui,target,height,active,colors.border));
  background.addState(new int[]{android.R.attr.state_focused},pill(ui,target,height,active,colors.border));
  background.addState(new int[]{android.R.attr.state_hovered},pill(ui,target,height,active,colors.border));
  background.addState(new int[]{},pill(ui,target,height,fill,ThemeColors.transparent(colors.border,22)));
  handle.setBackground(background);
  FrameLayout.LayoutParams params=new FrameLayout.LayoutParams(ui.dp(Math.max(64,target)),ui.dp(target),Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL);frame.addView(handle,params);
  handle.setOnTouchListener(new View.OnTouchListener(){float x,y;boolean fired;
   public boolean onTouch(View v,MotionEvent event){
    AccessibilityManager am=(AccessibilityManager)owner.getSystemService(Activity.ACCESSIBILITY_SERVICE);
    if(am!=null&&am.isTouchExplorationEnabled())return false;
    if(event.getActionMasked()==MotionEvent.ACTION_DOWN){x=event.getX();y=event.getY();fired=false;}
    else if(event.getActionMasked()==MotionEvent.ACTION_MOVE&&!fired&&event.getPointerCount()==1&&y-event.getY()>ui.dp(24)&&Math.abs(event.getX()-x)<ui.dp(VrProfile.target(owner,48))){fired=true;v.setPressed(false);open.run();return true;}
    else if(event.getActionMasked()==MotionEvent.ACTION_UP&&fired){fired=false;v.setPressed(false);return true;}
    else if(event.getActionMasked()==MotionEvent.ACTION_CANCEL){fired=false;v.setPressed(false);}
    return fired;
   }
  });
 }
 private static Drawable pill(SettingsUi ui,int target,int height,int fill,int border){
  GradientDrawable shape=new GradientDrawable();shape.setColor(fill);shape.setCornerRadius(ui.dp(height/2));shape.setStroke(ui.dp(1),border);
  int inset=ui.dp((target-height)/2);
  return new InsetDrawable(shape,ui.dp(4),inset,ui.dp(4),inset);
 }
 private BottomActionDrawer(){}
}
