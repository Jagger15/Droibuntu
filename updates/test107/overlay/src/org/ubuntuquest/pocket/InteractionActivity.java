// SPDX-License-Identifier: GPL-3.0-or-later
package org.ubuntuquest.pocket;
import android.app.Activity;
import android.hardware.input.InputManager;
import android.hardware.display.DisplayManager;
import android.os.*;
import android.view.*;
import android.view.accessibility.AccessibilityManager;

/** Input helpers are local to visible Droibuntu activities. No global interception. */
public class InteractionActivity extends RotatingActivity implements InputManager.InputDeviceListener,DisplayManager.DisplayListener {
 private String profile="";private boolean watching,edge,claimed;private float x,y;private long down;
 private final Handler interactionHandler=new Handler();
 private final Runnable refresh=()->{if(watching&&!isFinishing()&&!isDestroyed()){String next=DeviceProfiles.signature(this);if(!profile.equals(next)){profile=next;onConvergenceChanged();}Convergence.publish(this);}};
 protected void onConvergenceChanged(){Convergence.controls(this,getWindow().getDecorView());getWindow().getDecorView().requestLayout();}
 public void onConfigurationChanged(android.content.res.Configuration c){super.onConfigurationChanged(c);onConvergenceChanged();Convergence.publish(this);}
 private final Runnable viewport=new Runnable(){public void run(){if(watching){Convergence.publish(InteractionActivity.this);interactionHandler.postDelayed(this,3000);}}};
 protected void onCreate(Bundle b){super.onCreate(b);profile=DeviceProfiles.signature(this);}
 protected void onResume(){super.onResume();if(this instanceof MainActivity||this instanceof PhoneHomeActivity)BottomActionDrawer.install(this,this::showContextActions,showBottomActionHandle());watching=true;((InputManager)getSystemService(INPUT_SERVICE)).registerInputDeviceListener(this,interactionHandler);((DisplayManager)getSystemService(DISPLAY_SERVICE)).registerDisplayListener(this,interactionHandler);interactionHandler.post(refresh);interactionHandler.post(viewport);}
 protected void onPause(){Convergence.release(getWindow().getDecorView());QuietUndo.dismiss(this);watching=false;edge=false;interactionHandler.removeCallbacks(refresh);interactionHandler.removeCallbacks(viewport);((InputManager)getSystemService(INPUT_SERVICE)).unregisterInputDeviceListener(this);((DisplayManager)getSystemService(DISPLAY_SERVICE)).unregisterDisplayListener(this);super.onPause();}
 public void onInputDeviceAdded(int id){changed();}public void onInputDeviceRemoved(int id){Convergence.release(getWindow().getDecorView());changed();}public void onInputDeviceChanged(int id){changed();}
 public void onDisplayAdded(int id){changed();}public void onDisplayRemoved(int id){changed();}public void onDisplayChanged(int id){changed();}
 private void changed(){interactionHandler.removeCallbacks(refresh);interactionHandler.postDelayed(refresh,300);}
 public boolean dispatchKeyEvent(KeyEvent e){
  if(e.getAction()==KeyEvent.ACTION_DOWN&&e.getRepeatCount()==0){
   if(e.isAltPressed()&&e.getKeyCode()==KeyEvent.KEYCODE_SPACE){InteractionActions.open(this,ActionSearchActivity.class);return true;}
   if(e.isCtrlPressed()&&e.isAltPressed()&&e.getKeyCode()==KeyEvent.KEYCODE_R){InteractionActions.open(this,RunningAppsActivity.class);return true;}
   if(e.isCtrlPressed()&&e.isAltPressed()&&e.getKeyCode()==KeyEvent.KEYCODE_S){InteractionActions.open(this,StatusCenterActivity.class);return true;}
  }return super.dispatchKeyEvent(e);
 }
 public void onContentChanged(){super.onContentChanged();if(this instanceof MainActivity||this instanceof PhoneHomeActivity)BottomActionDrawer.install(this,this::showContextActions,showBottomActionHandle());}
 protected boolean showBottomActionHandle(){return true;}
 protected void showContextActions(){InteractionActions.run(this,"status");}
 protected boolean allowEdgeGestures(){return true;}
 public boolean dispatchTouchEvent(MotionEvent e){
  if(e.getActionMasked()==MotionEvent.ACTION_DOWN){claimed=false;
   edge=false;View decor=getWindow().getDecorView();float d=getResources().getDisplayMetrics().density;AccessibilityManager am=(AccessibilityManager)getSystemService(ACCESSIBILITY_SERVICE);
   // Keep Android's outer back-gesture strip free. Only the inner 32–64 dp band is eligible.
   float px=e.getX(),py=e.getY();edge=allowEdgeGestures()&&DeviceProfiles.settings(this).getBoolean("edges",false)&&e.getPointerCount()==1&&e.getToolType(0)==MotionEvent.TOOL_TYPE_FINGER&&!(am!=null&&am.isTouchExplorationEnabled())&&(px>=32*d&&px<=64*d||px>=decor.getWidth()-64*d&&px<=decor.getWidth()-32*d)&&py>=96*d&&py<=decor.getHeight()-96*d;
   x=px;y=py;down=e.getEventTime();
  }else if(e.getPointerCount()>1||e.getActionMasked()==MotionEvent.ACTION_CANCEL)edge=false;
  else if(edge&&e.getActionMasked()==MotionEvent.ACTION_MOVE){
   float d=getResources().getDisplayMetrics().density;int sensitivity=DeviceProfiles.settings(this).getInt("edge-distance",96);float dx=e.getX()-x;boolean left=x<getWindow().getDecorView().getWidth()/2f;
   if(e.getEventTime()-down<1000&&(left?dx>0:dx<0)&&DeviceProfilePolicy.swipe(dx,e.getY()-y,Math.max(64,Math.min(160,sensitivity))*d)){
    edge=false;claimed=true;MotionEvent cancel=MotionEvent.obtain(e);cancel.setAction(MotionEvent.ACTION_CANCEL);super.dispatchTouchEvent(cancel);cancel.recycle();
    InteractionActions.run(this,DeviceProfiles.settings(this).getString(left?"edge-left":"edge-right",left?"apps":"running"));return true;
   }
  }else if(e.getActionMasked()==MotionEvent.ACTION_UP)edge=false;
  if(claimed)return true;
  return super.dispatchTouchEvent(e);
 }
}
