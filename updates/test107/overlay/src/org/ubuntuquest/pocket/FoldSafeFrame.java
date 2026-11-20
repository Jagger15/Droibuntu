// SPDX-License-Identifier: GPL-3.0-or-later
package org.ubuntuquest.pocket;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.widget.FrameLayout;
import androidx.core.util.Consumer;
import androidx.window.java.layout.WindowInfoTrackerCallbackAdapter;
import androidx.window.layout.*;

/** Re-measures the same live content inside a hinge-free pane, without restarting Ubuntu. */
final class FoldSafeFrame extends FrameLayout {
 private final Activity owner;
 private WindowInfoTrackerCallbackAdapter tracker;
 private FoldingFeature fold;
 private boolean listening;
 private final Consumer<WindowLayoutInfo> updates=info->{
  fold=null;
  for(DisplayFeature feature:info.getDisplayFeatures())if(feature instanceof FoldingFeature){
   FoldingFeature candidate=(FoldingFeature)feature;
   if(candidate.isSeparating()||candidate.getOcclusionType()==FoldingFeature.OcclusionType.FULL){fold=candidate;break;}
  }
  applyPane();
 };
 FoldSafeFrame(Activity owner){super(owner);this.owner=owner;setClipToPadding(true);}
 protected void onAttachedToWindow(){
  super.onAttachedToWindow();listening=true;
  try{
   tracker=new WindowInfoTrackerCallbackAdapter(WindowInfoTracker.getOrCreate(owner));
   tracker.addWindowLayoutInfoListener(owner,command->post(()->{if(listening)command.run();}),updates);
  }catch(RuntimeException|LinkageError unavailable){
   android.util.Log.w("Droibuntu","Fold information unavailable; using window size",unavailable);
  }
  post(this::applyPane);
 }
 protected void onDetachedFromWindow(){
  listening=false;
  if(tracker!=null){tracker.removeWindowLayoutInfoListener(updates);tracker=null;}
  fold=null;super.onDetachedFromWindow();
 }
 protected void onSizeChanged(int w,int h,int oldW,int oldH){super.onSizeChanged(w,h,oldW,oldH);post(this::applyPane);}
 protected void onLayout(boolean changed,int l,int t,int r,int b){super.onLayout(changed,l,t,r,b);if(changed)post(this::applyPane);}
 private void applyPane(){
  if(!isAttachedToWindow())return;
  int[] pane={0,0,getWidth(),getHeight()};
  if(fold!=null){
   Rect bounds=new Rect(fold.getBounds());
   // WindowManager coordinates belong to the activity window, also for modal sheets.
   int[] window=new int[2],here=new int[2];owner.getWindow().getDecorView().getLocationOnScreen(window);getLocationOnScreen(here);
   bounds.offset(window[0]-here[0],window[1]-here[1]);
   pane=FoldLayoutPolicy.safePane(getWidth(),getHeight(),bounds.left,bounds.top,bounds.right,bounds.bottom,
     fold.getOrientation()==FoldingFeature.Orientation.VERTICAL,true);
  }
  int right=getWidth()-pane[2],bottom=getHeight()-pane[3];
  if(getPaddingLeft()!=pane[0]||getPaddingTop()!=pane[1]||getPaddingRight()!=right||getPaddingBottom()!=bottom){
   setPadding(pane[0],pane[1],right,bottom);
   post(()->FocusViewport.reveal(this));
  }
 }
}
