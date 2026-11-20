// SPDX-License-Identifier: GPL-3.0-or-later
package org.ubuntuquest.pocket;

import android.app.Activity;
import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.view.*;
import android.widget.*;

/** Launcher-owned, themed sheet; all actions remain native Android callbacks. */
final class LauncherMenuSheet extends Dialog {
 private ControllerNavigation controller;
 private final Activity activity;private final ThemeColors colors;private final LinearLayout entries,card;
 private final int transparency;
 private int dp(int value){return Math.round(value*getContext().getResources().getDisplayMetrics().density);}
 private GradientDrawable shape(int fill,int radius){GradientDrawable d=new GradientDrawable();d.setColor(fill);d.setCornerRadius(dp(radius));return d;}
 private TextView label(String text,int size,int color){TextView v=new TextView(getContext());v.setText(text);v.setTextSize(VrProfile.text(activity,size));v.setTextColor(color);v.setShadowLayer(dp(2),0,dp(1),0x99000000);return v;}
 private Drawable icon(String name){Drawable d=activity.getDrawable(activity.getResources().getIdentifier(name,"drawable",activity.getPackageName())).mutate();d.setTint(colors.border);return d;}
 private Drawable focused(){GradientDrawable d=shape(0x32ffffff,12);if(VrProfile.outline(activity))d.setStroke(dp(3),Color.WHITE);return d;}
 private Drawable rowBackground(){
  StateListDrawable states=new StateListDrawable();states.addState(new int[]{android.R.attr.state_focused},focused());states.addState(new int[]{android.R.attr.state_hovered},shape(0x20ffffff,12));states.addState(new int[]{},shape(Color.TRANSPARENT,12));
  return new RippleDrawable(ColorStateList.valueOf(0x35ffffff),states,shape(Color.WHITE,12));
 }
 LauncherMenuSheet(Activity owner){this(owner,"Launcher options");}
 LauncherMenuSheet(Activity owner,String menuTitle){this(owner,menuTitle,"launcher");}
 LauncherMenuSheet(Activity owner,String menuTitle,String themeTarget){
  super(owner);activity=owner;colors=ThemeColors.read(owner,themeTarget);transparency=ThemeColors.transparency(owner);
  requestWindowFeature(Window.FEATURE_NO_TITLE);setCancelable(true);
  SystemBarFrame outside=new SystemBarFrame(owner,false,true);outside.setOnClickListener(v->dismiss());
  card=new LinearLayout(owner){protected void onMeasure(int widthSpec,int heightSpec){
   int width=Math.min(MeasureSpec.getSize(widthSpec),dp(520));
   int height=MeasureSpec.getSize(heightSpec);int cap=Math.min(dp(760),Math.max(dp(120),(int)(height*.88f)));
   super.onMeasure(MeasureSpec.makeMeasureSpec(width,MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(cap,MeasureSpec.AT_MOST));
  }};
  card.setOrientation(LinearLayout.VERTICAL);card.setPadding(dp(12),dp(8),dp(12),dp(12));card.setClickable(true);card.setClipToOutline(true);
  // Modal menus need their own opacity floor so launcher labels cannot compete with actions.
  GradientDrawable panel=shape(ThemeColors.transparent(colors.panel,Math.min(15,transparency)),24);panel.setStroke(dp(1),ThemeColors.transparent(colors.border,Math.min(80,transparency)));card.setBackground(panel);
  FrameLayout.LayoutParams position=new FrameLayout.LayoutParams(-1,-2,Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL);position.setMargins(dp(12),dp(8),dp(12),dp(8));outside.addView(card,position);
  LinearLayout heading=new LinearLayout(owner);heading.setGravity(Gravity.CENTER_VERTICAL);heading.setPadding(dp(8),dp(4),0,dp(4));card.addView(heading,new LinearLayout.LayoutParams(-1,-2));
  TextView title=label(menuTitle,21,Color.WHITE);title.setTypeface(null,Typeface.BOLD);title.setMaxLines(2);title.setEllipsize(android.text.TextUtils.TruncateAt.END);if(Build.VERSION.SDK_INT>=28)title.setAccessibilityHeading(true);heading.addView(title,new LinearLayout.LayoutParams(0,-2,1));
  ImageButton close=new ImageButton(owner);close.setImageDrawable(icon("ic_menu_close"));close.setPadding(dp(13),dp(13),dp(13),dp(13));close.setScaleType(ImageView.ScaleType.FIT_CENTER);close.setBackground(rowBackground());close.setContentDescription("Close "+menuTitle);close.setTooltipText("Close");close.setOnClickListener(v->dismiss());heading.addView(close,new LinearLayout.LayoutParams(dp(VrProfile.target(activity,48)),dp(VrProfile.target(activity,48))));
  ScrollView scroll=new ScrollView(owner);scroll.setFillViewport(false);scroll.setClipToPadding(false);scroll.setVerticalScrollBarEnabled(true);card.addView(scroll,new LinearLayout.LayoutParams(-1,-2,1));
  entries=new LinearLayout(owner);entries.setOrientation(LinearLayout.VERTICAL);scroll.addView(entries,new ScrollView.LayoutParams(-1,-2));
  FoldSafeFrame foldFrame=new FoldSafeFrame(owner);foldFrame.addView(outside,new FrameLayout.LayoutParams(-1,-1));setContentView(foldFrame);Window window=getWindow();window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND|WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);window.setDimAmount(.60f);
  window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
  window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
  window.setStatusBarColor(Color.TRANSPARENT);window.setNavigationBarColor(Color.TRANSPARENT);
  if(Build.VERSION.SDK_INT>=28)window.setNavigationBarDividerColor(Color.TRANSPARENT);
  if(Build.VERSION.SDK_INT>=29){window.setStatusBarContrastEnforced(false);window.setNavigationBarContrastEnforced(false);}
  setOnShowListener(d->{window.setLayout(-1,-1);outside.requestApplyInsets();});
 }
 private ControllerNavigation controller(){if(controller==null)controller=new ControllerNavigation((ViewGroup)getWindow().getDecorView(),e->super.dispatchKeyEvent(e),this::dismiss,()->{});return controller;}
 protected void onStart(){super.onStart();controller().start();}
 protected void onStop(){if(controller!=null)controller.stop();super.onStop();}
 public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(!focused&&controller!=null)controller.reset();}
 public boolean dispatchKeyEvent(KeyEvent event){return controller().key(event)||super.dispatchKeyEvent(event);}
 public boolean dispatchGenericMotionEvent(MotionEvent event){return controller().motion(event)||super.dispatchGenericMotionEvent(event);}
 public boolean dispatchTouchEvent(MotionEvent event){if(event.getActionMasked()==MotionEvent.ACTION_DOWN)controller().touch();return super.dispatchTouchEvent(event);}
 void content(View view){entries.addView(view,new LinearLayout.LayoutParams(-1,-2));}
 void footer(View view){card.addView(view,new LinearLayout.LayoutParams(-1,-2));}
 void enableInput(){
  // Let the window resize above the keyboard; ordinary action sheets keep their overlay behavior.
  getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
  getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
 }
 void section(String title){
  TextView heading=label(title,12,colors.border);heading.setTypeface(null,Typeface.BOLD);heading.setPadding(dp(12),dp(14),dp(12),dp(6));if(Build.VERSION.SDK_INT>=28)heading.setAccessibilityHeading(true);entries.addView(heading,new LinearLayout.LayoutParams(-1,-2));
 }
 void action(String title,String iconName,Runnable action){action(title,icon(iconName),action);}
 void action(String title,Drawable rowIcon,Runnable action){
  LinearLayout row=new LinearLayout(getContext());row.setOrientation(LinearLayout.HORIZONTAL);row.setGravity(Gravity.CENTER_VERTICAL);row.setMinimumHeight(dp(VrProfile.target(activity,52)));row.setPadding(dp(12),dp(10),dp(12),dp(10));row.setBackground(rowBackground());row.setFocusable(true);row.setClickable(true);row.setContentDescription(title);
  ImageView image=new ImageView(getContext());image.setImageDrawable(rowIcon);image.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);row.addView(image,new LinearLayout.LayoutParams(dp(22),dp(22)));
  TextView text=label(title,16,Color.WHITE);text.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-2,1);p.setMarginStart(dp(16));row.addView(text,p);
  row.setOnClickListener(v->{dismiss();action.run();});entries.addView(row,new LinearLayout.LayoutParams(-1,-2));
 }
 void pinnableAction(String title,Drawable icon,Runnable open,Runnable pin){
  action(title,icon,open);View row=entries.getChildAt(entries.getChildCount()-1);row.setTooltipText("Hold to pin "+title+" to Home");row.setOnLongClickListener(v->{dismiss();pin.run();return true;});
  row.setAccessibilityDelegate(new View.AccessibilityDelegate(){public void onInitializeAccessibilityNodeInfo(View host,android.view.accessibility.AccessibilityNodeInfo info){super.onInitializeAccessibilityNodeInfo(host,info);info.addAction(new android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction(0x01021020,"Pin action to Home"));}public boolean performAccessibilityAction(View host,int action,android.os.Bundle args){if(action==0x01021020){dismiss();pin.run();return true;}return super.performAccessibilityAction(host,action,args);}});
 }
 void pinnableAction(String title,String icon,Runnable open,Runnable pin){pinnableAction(title,icon(icon),open,pin);}
 void choice(String title,String iconName,boolean checked,Runnable select){
  checkedRow(title,iconName,checked,true,value->{dismiss();select.run();});
 }
 void toggle(String title,String iconName,boolean checked,java.util.function.Consumer<Boolean> select){
  checkedRow(title,iconName,checked,false,select);
 }
 private void checkedRow(String title,String iconName,boolean checked,boolean radio,java.util.function.Consumer<Boolean> select){
  LinearLayout row=new LinearLayout(getContext());row.setGravity(Gravity.CENTER_VERTICAL);row.setMinimumHeight(dp(VrProfile.target(activity,52)));row.setPadding(dp(12),dp(8),dp(12),dp(8));row.setBackground(rowBackground());row.setFocusable(true);row.setClickable(true);row.setContentDescription(title);
  ImageView image=new ImageView(getContext());image.setImageDrawable(icon(iconName));image.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);row.addView(image,new LinearLayout.LayoutParams(dp(22),dp(22)));
  TextView text=label(title,16,Color.WHITE);text.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);LinearLayout.LayoutParams textParams=new LinearLayout.LayoutParams(0,-2,1);textParams.setMarginStart(dp(16));row.addView(text,textParams);
  CompoundButton indicator=radio?new RadioButton(getContext()):new CheckBox(getContext());indicator.setChecked(checked);indicator.setButtonTintList(new ColorStateList(new int[][]{new int[]{android.R.attr.state_checked},new int[]{}},new int[]{colors.accent,colors.border}));indicator.setClickable(false);indicator.setFocusable(false);indicator.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);indicator.setBackground(null);row.addView(indicator,new LinearLayout.LayoutParams(dp(40),dp(40)));
  row.setAccessibilityDelegate(new View.AccessibilityDelegate(){public void onInitializeAccessibilityNodeInfo(View host,android.view.accessibility.AccessibilityNodeInfo info){super.onInitializeAccessibilityNodeInfo(host,info);info.setClassName(radio?"android.widget.RadioButton":"android.widget.CheckBox");info.setCheckable(true);info.setChecked(indicator.isChecked());}});
  row.setOnClickListener(v->{boolean value=radio||!indicator.isChecked();indicator.setChecked(value);select.accept(value);row.sendAccessibilityEvent(android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED);});entries.addView(row,new LinearLayout.LayoutParams(-1,-2));
 }

}
