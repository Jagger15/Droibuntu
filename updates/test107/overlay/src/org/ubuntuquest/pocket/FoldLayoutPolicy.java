// SPDX-License-Identifier: GPL-3.0-or-later
package org.ubuntuquest.pocket;

/** Pure pixel geometry; a zero-width crease still separates two usable panes. */
final class FoldLayoutPolicy {
 static int[] safePane(int width,int height,int left,int top,int right,int bottom,
                       boolean vertical,boolean separating){
  int[] full={0,0,width,height};
  if(!separating||width<=0||height<=0)return full;
  if(vertical){
   if(bottom<=0||top>=height||left<=0||right>=width||right<left)return full;
   return left>=width-right?new int[]{0,0,left,height}:new int[]{right,0,width,height};
  }
  if(right<=0||left>=width||top<=0||bottom>=height||bottom<top)return full;
  // Tabletop: keep the complete workspace on the upright, upper display.
  return new int[]{0,0,width,top};
 }
 private FoldLayoutPolicy(){}
}
