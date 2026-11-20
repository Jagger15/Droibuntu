// SPDX-License-Identifier: GPL-3.0-or-later
package org.ubuntuquest.pocket;
import android.app.Activity;
import android.content.res.Configuration;

/** Keep live inputs, jobs, selection and scroll positions while native views remeasure. */
public class RotatingActivity extends Activity {
 private ControllerNavigation controller;
 private int viewportRevision;private PageMemory pageMemory;
 protected void onCreate(android.os.Bundle state){super.onCreate(state);pageMemory=new PageMemory(this,state==null?null:state.getBundle("droibuntu-pages"));}
 public void onContentChanged(){super.onContentChanged();bindPage();}
 private void bindPage(){if(pageMemory!=null){android.view.View root=getWindow().getDecorView();root.post(()->{if(!isDestroyed())pageMemory.bind(root);});}}
 public void setContentView(android.view.View view){setContentView(view,new android.view.ViewGroup.LayoutParams(-1,-1));}
 public void setContentView(android.view.View view,android.view.ViewGroup.LayoutParams params){
  FoldSafeFrame frame=new FoldSafeFrame(this);frame.addView(view,params);super.setContentView(frame);
 }
 public void setContentView(int layout){setContentView(getLayoutInflater().inflate(layout,null));}
 private ControllerNavigation controller(){
  if(controller==null)controller=new ControllerNavigation((android.view.ViewGroup)getWindow().getDecorView(),e->super.dispatchKeyEvent(e),this::onBackPressed,()->{if(this instanceof InteractionActivity)((InteractionActivity)this).showContextActions();});
  return controller;
 }
 public boolean dispatchKeyEvent(android.view.KeyEvent event){return controller().key(event)||super.dispatchKeyEvent(event);}
 public boolean dispatchGenericMotionEvent(android.view.MotionEvent event){return controller().motion(event)||super.dispatchGenericMotionEvent(event);}
 public boolean dispatchTouchEvent(android.view.MotionEvent event){if(event.getActionMasked()==android.view.MotionEvent.ACTION_DOWN)controller().touch();return super.dispatchTouchEvent(event);}
 public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(!focused&&controller!=null)controller.reset();}
 protected void onResume(){super.onResume();controller().start();bindPage();}
 protected void onPause(){if(controller!=null)controller.stop();if(pageMemory!=null)pageMemory.capture();super.onPause();}
 protected void onSaveInstanceState(android.os.Bundle state){if(pageMemory!=null)state.putBundle("droibuntu-pages",pageMemory.save());super.onSaveInstanceState(state);}

 public void onConfigurationChanged(Configuration configuration){
  super.onConfigurationChanged(configuration);
  android.view.View root=getWindow().getDecorView();
  root.requestApplyInsets();root.requestLayout();
  final int revision=++viewportRevision;
  Runnable reveal=()->{if(revision==viewportRevision&&!isFinishing()&&!isDestroyed())FocusViewport.reveal(root);};
  root.post(reveal);root.postDelayed(reveal,250);
 }
}
