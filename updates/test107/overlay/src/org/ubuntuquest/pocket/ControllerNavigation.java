// SPDX-License-Identifier: GPL-3.0-or-later
package org.ubuntuquest.pocket;

import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.hardware.input.InputManager;
import android.os.*;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import java.util.function.Predicate;

/** Per-window navigation: modal sheets never send input to the activity behind them. */
final class ControllerNavigation implements InputManager.InputDeviceListener {
 private final ViewGroup root;
 private final Predicate<KeyEvent> dispatch;
 private final Runnable back,actions;
 private final Handler handler=new Handler(Looper.getMainLooper());
 private boolean running,visible,backArmed;
 private int direction,device=-1;
 private View pressed;
 private final Drawable ring=new Drawable(){
  private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
  public void draw(Canvas canvas){paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(3*root.getResources().getDisplayMetrics().density);paint.setColor(Color.WHITE);Rect b=getBounds();canvas.drawRoundRect(b.left+2,b.top+2,b.right-2,b.bottom-2,8,8,paint);}
  public void setAlpha(int alpha){}public void setColorFilter(ColorFilter filter){}public int getOpacity(){return PixelFormat.TRANSLUCENT;}
 };
 private final ViewTreeObserver.OnPreDrawListener highlight=()->{updateRing();return true;};
 private final Runnable repeat=new Runnable(){public void run(){if(!running||!root.hasWindowFocus()||direction==0){stopStick();return;}step(direction);handler.postDelayed(this,150);}};
 ControllerNavigation(ViewGroup root,Predicate<KeyEvent> dispatch,Runnable back,Runnable actions){this.root=root;this.dispatch=dispatch;this.back=back;this.actions=actions;}
 void start(){if(running)return;running=true;root.getViewTreeObserver().addOnPreDrawListener(highlight);((InputManager)root.getContext().getSystemService(android.content.Context.INPUT_SERVICE)).registerInputDeviceListener(this,handler);}
 void stop(){if(!running)return;running=false;reset();root.getViewTreeObserver().removeOnPreDrawListener(highlight);((InputManager)root.getContext().getSystemService(android.content.Context.INPUT_SERVICE)).unregisterInputDeviceListener(this);}
 void reset(){stopStick();pressed=null;backArmed=false;visible=false;root.getOverlay().remove(ring);}
 void touch(){reset();}
 private void stopStick(){handler.removeCallbacks(repeat);direction=0;device=-1;}
 private boolean gamepad(KeyEvent event){return event.isFromSource(InputDevice.SOURCE_GAMEPAD)||event.isFromSource(InputDevice.SOURCE_JOYSTICK);}
 private void prepare(){visible=true;root.getOverlay().add(ring);View focus=root.findFocus();if(focus==null||focus==root||(!(focus instanceof DesktopView)&&!(focus instanceof EditText)&&!focus.isClickable())){
   java.util.ArrayList<View> candidates=new java.util.ArrayList<>();
   root.addFocusables(candidates,View.FOCUS_FORWARD,View.FOCUSABLES_ALL);
   for(View candidate:candidates)if(candidate.isShown()&&candidate.isEnabled()&&(candidate.isClickable()||candidate instanceof EditText||candidate instanceof DesktopView)){candidate.requestFocusFromTouch();break;}
  } }
 boolean key(KeyEvent event){
  int key=event.getKeyCode();boolean pad=gamepad(event);
  if(key>=KeyEvent.KEYCODE_DPAD_UP&&key<=KeyEvent.KEYCODE_DPAD_CENTER){prepare();return false;}
  if(!pad)return false;
  prepare();View focus=root.findFocus();
  // Preserve the existing Quest desktop mouse mappings; native menus still use this class.
  if(focus instanceof DesktopView&&VrProfile.buttons(root.getContext()))return false;
  if(key==KeyEvent.KEYCODE_BUTTON_A){
   if(event.getAction()==KeyEvent.ACTION_DOWN&&event.getRepeatCount()==0)pressed=focus;
   if(event.getAction()==KeyEvent.ACTION_UP){View target=pressed;pressed=null;if(!event.isCanceled()&&target!=null&&target==focus&&target.isShown()&&target.isEnabled()){
    if(target instanceof DesktopView)((DesktopView)target).tap(0xff0d);
    else if(target instanceof EditText)((InputMethodManager)root.getContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE)).showSoftInput(target,InputMethodManager.SHOW_IMPLICIT);
    else target.performClick();
   }}return true;
  }
  if(key==KeyEvent.KEYCODE_BUTTON_B){
   if(event.getAction()==KeyEvent.ACTION_DOWN&&event.getRepeatCount()==0)backArmed=true;
   if(event.getAction()==KeyEvent.ACTION_UP){boolean armed=backArmed;backArmed=false;if(armed&&!event.isCanceled()){
    if(focus instanceof DesktopView)((DesktopView)focus).tap(0xff1b);
    else if(!hideKeyboard())back.run();
   }}return true;
  }
  if(key==KeyEvent.KEYCODE_BUTTON_X){if(event.getAction()==KeyEvent.ACTION_DOWN&&event.getRepeatCount()==0&&focus!=null){if(focus instanceof DesktopView)((DesktopView)focus).showKeyboard();else if(focus.isLongClickable())focus.performLongClick();}return true;}
  if(key==KeyEvent.KEYCODE_BUTTON_Y||key==KeyEvent.KEYCODE_BUTTON_START){if(event.getAction()==KeyEvent.ACTION_DOWN&&event.getRepeatCount()==0){stopStick();actions.run();}return true;}
  if(key==KeyEvent.KEYCODE_BUTTON_L1||key==KeyEvent.KEYCODE_BUTTON_R1){if(event.getAction()==KeyEvent.ACTION_DOWN&&event.getRepeatCount()==0){send(KeyEvent.KEYCODE_TAB,key==KeyEvent.KEYCODE_BUTTON_L1?KeyEvent.META_SHIFT_ON:0);}return true;}
  return false;
 }
 boolean motion(MotionEvent event){
  if(!event.isFromSource(InputDevice.SOURCE_JOYSTICK))return false;
  if(event.getActionMasked()==MotionEvent.ACTION_CANCEL){stopStick();return true;}
  if(event.getActionMasked()!=MotionEvent.ACTION_MOVE)return false;
  if(root.findFocus() instanceof DesktopView&&VrProfile.buttons(root.getContext()))return false;
  float x=axis(event,MotionEvent.AXIS_HAT_X),y=axis(event,MotionEvent.AXIS_HAT_Y);
  if(x==0&&y==0){x=axis(event,MotionEvent.AXIS_X);y=axis(event,MotionEvent.AXIS_Y);}
  int next=ControllerPolicy.direction(x,y,.25f);
  if(next==0){stopStick();return !(root.findFocus() instanceof DesktopView);}
  prepare();
  if(direction!=next||device!=event.getDeviceId()){stopStick();direction=next;device=event.getDeviceId();step(next);handler.postDelayed(repeat,400);}
  return !(root.findFocus() instanceof DesktopView);
 }
 private boolean hideKeyboard(){
  boolean shown=false;
  if(Build.VERSION.SDK_INT>=30){WindowInsets insets=root.getRootWindowInsets();shown=insets!=null&&insets.isVisible(WindowInsets.Type.ime());}
  else {Rect visibleFrame=new Rect();root.getWindowVisibleDisplayFrame(visibleFrame);shown=root.getHeight()-visibleFrame.height()>160*root.getResources().getDisplayMetrics().density;}
  if(shown)((InputMethodManager)root.getContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(root.getWindowToken(),0);
  return shown;
 }
 private float axis(MotionEvent event,int axis){InputDevice input=event.getDevice();InputDevice.MotionRange range=input==null?null:input.getMotionRange(axis,event.getSource());float value=event.getAxisValue(axis);return Math.abs(value)<=Math.max(.25f,range==null?0:range.getFlat())?0:value;}
 private void step(int key){send(key,0);}
 private void send(int key,int meta){long now=SystemClock.uptimeMillis();dispatch.test(new KeyEvent(now,now,KeyEvent.ACTION_DOWN,key,0,meta,-1,0,0,InputDevice.SOURCE_DPAD));dispatch.test(new KeyEvent(now,now,KeyEvent.ACTION_UP,key,0,meta,-1,0,0,InputDevice.SOURCE_DPAD));}
 private void updateRing(){View focus=root.findFocus();if(!visible||focus==null||focus instanceof DesktopView||!focus.isShown()){ring.setBounds(0,0,0,0);return;}Rect bounds=new Rect();if(!focus.getGlobalVisibleRect(bounds)){ring.setBounds(0,0,0,0);return;}int[] origin=new int[2];root.getLocationOnScreen(origin);bounds.offset(-origin[0],-origin[1]);if(!bounds.equals(ring.getBounds()))ring.setBounds(bounds);}
 public void onInputDeviceAdded(int id){}public void onInputDeviceChanged(int id){reset();}public void onInputDeviceRemoved(int id){reset();}
}
